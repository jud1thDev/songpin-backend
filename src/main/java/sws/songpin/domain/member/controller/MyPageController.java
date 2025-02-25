package sws.songpin.domain.member.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sws.songpin.domain.bookmark.service.BookmarkService;
import sws.songpin.domain.member.dto.request.PasswordUpdateRequestDto;
import sws.songpin.domain.member.dto.request.ProfileDeactivateRequestDto;
import sws.songpin.domain.member.dto.request.ProfileUpdateRequestDto;
import sws.songpin.domain.member.service.ProfileService;
import sws.songpin.domain.model.SortBy;
import sws.songpin.domain.pin.service.PinService;
import sws.songpin.domain.playlist.service.PlaylistService;
import sws.songpin.global.auth.CookieUtil;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

@Tag(name = "MyPage", description = "MyPage 관련 API입니다.")
@RestController
@RequiredArgsConstructor
@RequestMapping("/me")
public class MyPageController {
    private final PlaylistService playlistService;
    private final BookmarkService bookmarkService;
    private final ProfileService profileService;
    private final PinService pinService;
    private final CookieUtil cookieUtil;

    @Operation(summary = "내 플레이리스트 목록 조회", description = "마이페이지에서 내 플레이리스트 목록 조회")
    @GetMapping("/playlists")
    public ResponseEntity<?> getAllPlaylists(){
        return ResponseEntity.ok(playlistService.getMemberPlaylists());
    }

    @Operation(summary = "내 북마크 목록 조회", description = "마이페이지에서 북마크 목록 조회")
    @GetMapping("/bookmarks")
    public ResponseEntity<?> getAllBookmarks(){
        return ResponseEntity.ok(bookmarkService.getAllBookmarks());
    }

    @Operation(summary = "내 정보 조회", description = "로그인한 사용자의 프로필 이미지, 닉네임, 아이디 정보 조회")
    @GetMapping
    public ResponseEntity<?> getMyProfile(){
        return ResponseEntity.ok(profileService.getMyProfile());
    }

    @Operation(summary = "프로필 편집", description = "프로필 이미지, 닉네임, 핸들 변경")
    @PatchMapping
    public ResponseEntity<?> updateProfile(@RequestBody @Valid ProfileUpdateRequestDto requestDto){
        profileService.updateProfile(requestDto);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "내 핀 피드 조회", description = "현재 사용자의 모든 핀 피드를 조회합니다.")
    @GetMapping("/feed")
    public ResponseEntity<?> getMyPinFeed(@PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(pinService.getMyPinFeed(pageable));
    }

    @Operation(summary = "내 핀 피드 월별 조회", description = "현재 사용자의 핀 피드를 년/월별로 조회합니다.")
    @GetMapping("/calendar")
    public ResponseEntity<?> getMyFeedPinsByMonth(@RequestParam("year") int year, @RequestParam("month") int month) {
        return ResponseEntity.ok(pinService.getMyPinFeedForMonth(year, month));
    }

    @Operation(summary = "마이페이지에서 핀 검색", description = "마이페이지에서의 핀 검색 결과를 페이징으로 불러옵니다.")
    @GetMapping("/pins")
    public ResponseEntity<?> songSearch(@RequestParam("keyword") final String keyword,
                                        @PageableDefault(size = 20) final Pageable pageable) throws UnsupportedEncodingException {
        String decodedKeyword = URLDecoder.decode(keyword, "UTF-8");
        return ResponseEntity.ok().body(pinService.searchMyPins(decodedKeyword, pageable));
    }

    @Operation(summary = "회원 탈퇴", description = "회원 상태를 '탈퇴'로 변경하고 닉네임을 '(알 수 없음)'으로 변경합니다. \t\n해당 회원의 handle을 랜덤 uuid 값으로 변경합니다. \t\nRedis와 쿠키에 저장되었던 회원의 Refresh Token을 삭제합니다. \t\n해당 회원이 등록했던 핀 등의 데이터는 남겨둡니다. \t\n해당 회원의 팔로우, 팔로잉 데이터는 삭제합니다.")
    @PatchMapping("/status")
    public ResponseEntity<?> deactivate(@Valid @RequestBody ProfileDeactivateRequestDto requestDto, HttpServletResponse response){
        profileService.deactivateProfile(requestDto);

        //쿠키 삭제
        cookieUtil.deleteCookie(response, "refreshToken");

        return ResponseEntity.ok().build();
    }

    @Operation(summary = "비밀번호 변경", description = "로그인한 회원의 비밀번호를 변경합니다.")
    @PatchMapping("/pw")
    public ResponseEntity<?> updatePassword(@RequestBody @Valid PasswordUpdateRequestDto requestDto){
        profileService.updatePassword(requestDto);
        return ResponseEntity.ok().build();
    }



}
