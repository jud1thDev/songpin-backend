package sws.songpin.domain.follow.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import sws.songpin.domain.follow.entity.Follow;
import sws.songpin.domain.member.entity.Member;

import java.util.List;
import java.util.Optional;

public interface FollowRepository extends JpaRepository<Follow,Long> {
    Boolean existsByFollowerAndFollowing(Member follower, Member following);
    List<Follow> findAllByFollowing(Member following);
    List<Follow> findAllByFollower(Member follower);
    Long countByFollowing(Member following);
    Long countByFollower(Member follower);
    Optional<Follow> findByFollowerAndFollowing(Member follower, Member following);
    void deleteAllByFollower(Member member);
    void deleteAllByFollowing(Member member);
}