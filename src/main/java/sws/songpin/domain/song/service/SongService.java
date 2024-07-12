package sws.songpin.domain.song.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sws.songpin.domain.genre.entity.Genre;
import sws.songpin.domain.genre.entity.GenreName;
import sws.songpin.domain.pin.repository.PinRepository;
import sws.songpin.domain.song.dto.request.SongAddRequestDto;
import sws.songpin.domain.song.dto.response.SongDetailsResponseDto;
import sws.songpin.domain.song.dto.response.SpotifySearchDto;
import sws.songpin.domain.song.entity.Song;
import sws.songpin.domain.song.repository.SongRepository;
import sws.songpin.domain.song.spotify.SpotifyUtil;
import se.michaelthelin.spotify.model_objects.specification.Track;
import sws.songpin.global.exception.CustomException;
import sws.songpin.global.exception.ErrorCode;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class SongService {

    private final SpotifyUtil spotifyUtil;
    private final SongRepository songRepository;
    private final PinRepository pinRepository;

    @Transactional(readOnly = true)
    public List<SpotifySearchDto> searchTracks(String keyword, int offset) {
        List<Track> tracks = spotifyUtil.searchTracks(keyword, offset);
        return tracks.stream()
                .map(track -> new SpotifySearchDto(
                        track.getName(),
                        spotifyUtil.getArtistNames(track),
                        track.getAlbum().getImages().length > 0 ? track.getAlbum().getImages()[0].getUrl() : null,
                        track.getId()
                ))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Optional<GenreName> calculateAvgGenreName(List<Genre> genres) {
        Map<GenreName, Long> genreCount = genres.stream()
                .collect(Collectors.groupingBy(Genre::getGenreName, Collectors.counting()));

        return genreCount.entrySet().stream()
                .sorted(Map.Entry.<GenreName, Long>comparingByValue().reversed()
                        .thenComparing(Map.Entry::getKey))
                .map(Map.Entry::getKey)
                .findFirst();
    }

    public Song createSong(SongAddRequestDto songRequestDto) {
        Song song = songRequestDto.toEntity();
        return songRepository.save(song);
    }

    @Transactional(readOnly = true)
    public Optional<Song> getSongByProviderTrackCode(String providerTrackCode) {
        return songRepository.findByProviderTrackCode(providerTrackCode);
    }

    public Song getOrCreateSong(SongAddRequestDto songRequestDto) {
        return getSongByProviderTrackCode(songRequestDto.providerTrackCode())
                .orElseGet(() -> createSong(songRequestDto));
    }

    @Transactional(readOnly = true)
    public SongDetailsResponseDto getSongDetails(Long songId) {
        Song song = songRepository.findById(songId)
                .orElseThrow(() -> new CustomException(ErrorCode.SONG_NOT_FOUND));
        int pinCount = pinRepository.countBySong(song);
        return SongDetailsResponseDto.from(song, pinCount);
    }
}
