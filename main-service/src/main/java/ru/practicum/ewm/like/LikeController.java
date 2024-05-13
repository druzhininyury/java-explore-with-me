package ru.practicum.ewm.like;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.practicum.ewm.like.dto.LikeDto;

@RestController
@RequiredArgsConstructor
public class LikeController {

    private final LikeService likeService;

    @PostMapping("/users/{userId}/events/{eventId}/likes")
    @ResponseStatus(HttpStatus.CREATED)
    public LikeDto addLike(@PathVariable long userId,
                           @PathVariable long eventId,
                           @RequestParam("positive") boolean positive) {
        return likeService.addLike(userId, eventId, positive);
    }

    @PatchMapping("/users/{userId}/events/{eventId}/likes")
    @ResponseStatus(HttpStatus.OK)
    public LikeDto updateLike(@PathVariable long userId,
                              @PathVariable long eventId,
                              @RequestParam("positive") boolean positive) {
        return likeService.updateLike(userId, eventId, positive);
    }

    @DeleteMapping("/users/{userId}/events/{eventId}/likes")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateLike(@PathVariable long userId, @PathVariable long eventId) {
        likeService.deleteLike(userId, eventId);
    }

}
