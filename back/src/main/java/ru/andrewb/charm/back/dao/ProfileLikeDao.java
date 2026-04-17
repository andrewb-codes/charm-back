package ru.andrewb.charm.back.dao;

import org.springframework.stereotype.Repository;
import ru.andrewb.charm.back.model.exception.BadRequestException;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

@Repository
public class ProfileLikeDao {

    private final DataSource dataSource;

    public ProfileLikeDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    //language=POSTGRES-PSQL
    private static final String LIKE = """
			INSERT INTO profile_like (a_profile, b_profile, liked_a, liked_b)
			VALUES (?, ?, ?, ?)
			ON CONFLICT (a_profile, b_profile) DO UPDATE
			SET liked_a = COALESCE(EXCLUDED.liked_a, profile_like.liked_a),
			    liked_b = COALESCE(EXCLUDED.liked_b, profile_like.liked_b),
			    updated_at = now()
			""";


    public void likeOrDislike(Long fromId, Long toId, boolean isLike) {
        if (fromId.equals(toId)) throw new BadRequestException("error.charm.self-like");

        long a = Math.min(fromId, toId);
        long b = Math.max(fromId, toId);

        Boolean likedA = (fromId == a) ? isLike : null;
        Boolean likedB = (fromId == b) ? isLike : null;

        try (Connection conn = dataSource.getConnection();
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
