package ru.practicum.ewm.like;

import ru.practicum.ewm.like.dto.LikeDto;

public interface LikeService {

    LikeDto addLike(long userId, long eventId, boolean positive);

    LikeDto updateLike(long userId, long eventId, boolean positive);

    void deleteLike(long userId, long eventId);

}
