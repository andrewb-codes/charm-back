package ru.andrewb.charm.back.dao;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.andrewb.charm.back.infra.db.ConnectionManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ProfileLikeDao {
    //language=POSTGRES-PSQL
    private static final String LIKE = """
			INSERT INTO profile_like (a_profile, b_profile, liked_a, liked_b)
			VALUES (?, ?, ?, ?)
			ON CONFLICT (a_profile, b_profile) DO UPDATE
			SET liked_a = COALESCE(EXCLUDED.liked_a, profile_like.liked_a),
			    liked_b = COALESCE(EXCLUDED.liked_b, profile_like.liked_b),
			    updated_at = now()
			""";
    

    private static final ProfileLikeDao INSTANCE = new ProfileLikeDao();

    public static ProfileLikeDao getInstance() {
        return INSTANCE;
    }

    public void likeOrDislike(Long fromId, Long toId, boolean isLike) {
        if (fromId.equals(toId)) throw new IllegalArgumentException("self-like is not allowed");

        long a = Math.min(fromId, toId);
        long b = Math.max(fromId, toId);

        Boolean likedA = (fromId == a) ? isLike : null;
        Boolean likedB = (fromId == b) ? isLike : null;

        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(LIKE)) {
            ps.setLong(1, a);
            ps.setLong(2, b);
            ps.setObject(3, likedA);
            ps.setObject(4, likedB);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
