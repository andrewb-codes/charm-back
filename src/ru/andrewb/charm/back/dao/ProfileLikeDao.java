package ru.andrewb.charm.back.dao;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.andrewb.charm.back.utils.ConnectionManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ProfileLikeDao {

    private static final ProfileLikeDao INSTANCE = new ProfileLikeDao();

    public static ProfileLikeDao getInstance() {
        return INSTANCE;
    }

    public void likeOrDislike(Long fromProfileId, Long toProfileId, boolean isLike) {
        //language=POSTGRES-PSQL
        String SQL_SELECT_REVERSE = """
                    SELECT l.*
                    FROM profile_like l
                    WHERE l.from_profile = ? AND l.to_profile = ?
                """;
        //language=POSTGRES-PSQL
        String SQL_INSERT = """
                    INSERT INTO profile_like (from_profile, to_profile, is_like, is_match)
                    VALUES (?, ?, ?, ?)
                    ON CONFLICT (from_profile, to_profile)
                    DO UPDATE SET is_like = ?, is_match = ?
                """;
        try (Connection conn = ConnectionManager.getConnection()) {

            boolean matchedNow = false;
            if (isLike) {
                try (PreparedStatement psReverse = conn.prepareStatement(SQL_SELECT_REVERSE)) {
                    psReverse.setLong(1, toProfileId);
                    psReverse.setLong(2, fromProfileId);
                    try (ResultSet rs = psReverse.executeQuery()) {
                        matchedNow = rs.next() && rs.getBoolean("is_like");
                    }
                }
            }

            try (PreparedStatement psInsert = conn.prepareStatement(SQL_INSERT)) {
                if (matchedNow) {
                    fillInsert(psInsert, fromProfileId, toProfileId, true, true);
                    psInsert.executeUpdate();
                    fillInsert(psInsert, toProfileId, fromProfileId, true, true);
                    psInsert.executeUpdate();
                } else {
                    fillInsert(psInsert, fromProfileId, toProfileId, isLike, false);
                    psInsert.executeUpdate();
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void fillInsert(PreparedStatement stmt, Long from, Long to, boolean isLike, boolean isMatch)
            throws SQLException {
        stmt.setLong(1, from);
        stmt.setLong(2, to);
        stmt.setBoolean(3, isLike);
        stmt.setBoolean(4, isMatch);
        stmt.setBoolean(5, isLike);
        stmt.setBoolean(6, isMatch);
    }
}
