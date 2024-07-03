package sws.songpin.entity.genre.domain;

import jakarta.persistence.*;
import lombok.*;
import sws.songpin.entity.songGenre.domain.SongGenre;
import sws.songpin.entity.pin.domain.Pin;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Entity
public class Genre {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "genre_id", updatable = false)
    private Long genreId;

    @Column(name = "genre_name", nullable = false)
    @Enumerated(EnumType.STRING)
    private GenreName genreName;

    @OneToMany(mappedBy = "genre", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Pin> pins;

    @OneToMany(mappedBy = "genre", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SongGenre> songGenres = new ArrayList<>();

    @Builder
    public Genre(Long genreId, GenreName genreName) {
        this.genreId = genreId;
        this.genreName = genreName;
        this.pins = new ArrayList<>();
        this.songGenres = new ArrayList<>();
    }
}
