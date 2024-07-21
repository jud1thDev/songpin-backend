package sws.songpin.domain.place.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sws.songpin.domain.genre.entity.GenreName;
import sws.songpin.domain.place.dto.request.MapBoundCoordsDto;
import sws.songpin.domain.place.dto.request.MapFetchEntirePeriodRequestDto;
import sws.songpin.domain.place.dto.request.MapFetchCustomPeriodRequestDto;
import sws.songpin.domain.place.dto.request.MapFetchRecentPeriodRequestDto;
import sws.songpin.domain.place.dto.response.MapPlaceFetchResponseDto;
import sws.songpin.domain.place.dto.response.MapPlaceProjectionDto;
import sws.songpin.domain.place.repository.MapPlaceRepository;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
@Slf4j
@Service
@Transactional(readOnly = true) // Transaction 모두 읽기 전용
@RequiredArgsConstructor
public class MapService {

    private final MapPlaceRepository mapPlaceRepository;

    // 장소 좌표들 가져오기-전체 기간 & 장르 필터링
    public MapPlaceFetchResponseDto getMapPlacesWithinBoundsByEntirePeriod(MapFetchEntirePeriodRequestDto requestDto) {
        List<GenreName> genreNameList =  getSelectedGenreNames(requestDto.genreNameFilters());
        Slice<MapPlaceProjectionDto> dtoSlice = getMapPlaceSlicesWithinBounds(requestDto.boundCoords(), genreNameList);
        return MapPlaceFetchResponseDto.from(dtoSlice);
    }

    // 장소 좌표들 가져오기-최근 기준 기간 & 장르 필터링
    public MapPlaceFetchResponseDto getMapPlacesWithinBoundsByRecentPeriod(MapFetchRecentPeriodRequestDto requestDto) {
        LocalDate startDate;
        LocalDate endDate = LocalDate.now();
        switch (requestDto.periodFilter()) {
            case "week" -> startDate = endDate.minusWeeks(1);
            case "month" -> startDate = endDate.minusMonths(1);
            case "threeMonths"-> startDate = endDate.minusMonths(3);
            default -> throw new IllegalArgumentException("Invalid period filter: " + requestDto.periodFilter());
        }
        List<GenreName> genreNameList =  getSelectedGenreNames(requestDto.genreNameFilters());
        Slice<MapPlaceProjectionDto> dtoSlice = getMapPlaceSlicesWithinBoundsAndDateRange(requestDto.boundCoords(), genreNameList, startDate, endDate);
        return MapPlaceFetchResponseDto.from(dtoSlice);
    }

    // 장소 좌표들 가져오기-기간 직접 설정 & 장르 필터링
    public MapPlaceFetchResponseDto getMapPlacesWithinBoundsByCustomPeriod(MapFetchCustomPeriodRequestDto requestDto) {
        List<GenreName> genreNameList =  getSelectedGenreNames(requestDto.genreNameFilters());
        Slice<MapPlaceProjectionDto> dtoSlice = getMapPlaceSlicesWithinBoundsAndDateRange(requestDto.boundCoords(), genreNameList, requestDto.startDate(), requestDto.endDate());
        return MapPlaceFetchResponseDto.from(dtoSlice);
    }

    // 날짜 범위 조건 걸지 않는 경우
    public Slice<MapPlaceProjectionDto> getMapPlaceSlicesWithinBounds(MapBoundCoordsDto dto, List<GenreName> genreNameList) {
        Pageable pageable = getCustomPageableForMap();
        return mapPlaceRepository.findPlacesWithLatestPinsByGenre(dto.swLat(), dto.neLat(), dto.swLng(), dto.neLng(), genreNameList, pageable);
    }

    // 날짜 범위 조건 거는 경우
    public Slice<MapPlaceProjectionDto> getMapPlaceSlicesWithinBoundsAndDateRange(MapBoundCoordsDto dto, List<GenreName> genreNameList, LocalDate startDate, LocalDate endDate) {
        Pageable pageable = getCustomPageableForMap();
        return mapPlaceRepository.findPlacesWithLatestPinsByGenreAndDateRange(dto.swLat(), dto.neLat(), dto.swLng(), dto.neLng(), genreNameList, startDate, endDate, pageable);
    }

    // 장르 필터링에 포함할 리스트 생성
    private List<GenreName> getSelectedGenreNames(List<GenreName> genreNameFilters) {
        if (genreNameFilters == null || genreNameFilters.isEmpty()) {
            return Arrays.asList(GenreName.values());
        }
        return genreNameFilters;
    }

    // 지도에 장소 좌표를 최대 100개 띄우도록 함
    private Pageable getCustomPageableForMap() {
        return PageRequest.of(0, 100);
    }

    //// 유저로 필터링
    // 유저가 핀을 등록한 장소 좌표들 가져오기
    public MapPlaceFetchResponseDto getMapPlacesOfMember(Long memberId) {
        Pageable pageable = PageRequest.of(0, 300);
        Slice<MapPlaceProjectionDto> dtoSlice = mapPlaceRepository.findPlacesWithLatestPinsByMember(memberId, pageable);
        return MapPlaceFetchResponseDto.from(dtoSlice);
    }
}