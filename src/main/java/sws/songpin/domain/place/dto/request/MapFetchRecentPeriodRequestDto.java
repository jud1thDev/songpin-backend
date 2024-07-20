package sws.songpin.domain.place.dto.request;

import jakarta.validation.constraints.NotNull;

import java.util.List;

public record MapFetchRecentPeriodRequestDto(
        @NotNull MapBoundCoordsDto boundCoords,
        List<String> genreNameFilters,
        @NotNull String periodFilter // "week", "month", "threeMonths"
) {
}
