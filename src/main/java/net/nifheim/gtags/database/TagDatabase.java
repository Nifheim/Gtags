package net.nifheim.gtags.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.io.Closeable;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;
import net.nifheim.gtags.Gtags;
import net.nifheim.gtags.tag.Tag;
import net.nifheim.gtags.tag.TagDesigner;
import org.bukkit.Bukkit;

/**
 * @author Beelzebu
 */
public class TagDatabase implements Closeable {

    private static final String SELECT_TAG_BY_NAME = "SELECT name, displayName, lore, permission, designer_id, BIN_TO_UUID(uniqueId, TRUE) AS uniqueId, username FROM tags LEFT JOIN designer USING (designer_id) WHERE name = ?;";
    private static final String SELECT_SELECTED_TAG = "SELECT name, displayName, lore, permission, designer_id, username, BIN_TO_UUID(designer.uniqueId, TRUE) as uniqueId  FROM selected JOIN tags USING (name) LEFT JOIN designer USING (designer_id) WHERE selected.uniqueId = UUID_TO_BIN(?, TRUE);";
    private static final String SELECT_ALL_TAGS = "SELECT name, displayName, lore, permission, designer_id, username, BIN_TO_UUID(uniqueId, TRUE) AS uniqueId FROM tags LEFT JOIN designer USING (designer_id) ORDER BY designer_id;";
    private static final String UPDATE_TAG = "UPDATE tags SET displayName = ?, permission = ? WHERE name = ?;";
    private static final String INSERT_TAG = "INSERT INTO tags VALUES (?, ?, ?, ?);";
    private static final String SET_PLAYER_TAG = "INSERT INTO selected VALUES (UUID_TO_BIN(?, TRUE), ?) ON DUPLICATE KEY UPDATE name = ?;";
    private final HikariDataSource dataSource;
    private final Executor executor;

    public TagDatabase(Gtags plugin, String host, int port, String database, String username, String password) {
        executor = r -> Bukkit.getScheduler().runTaskAsynchronously(plugin, r);
        HikariConfig config = new HikariConfig();
        config.setPoolName("Tags Connection Pool");
        config.setDataSourceClassName("org.mariadb.jdbc.MariaDbDataSource");
        config.addDataSourceProperty("serverName", host);
        config.addDataSourceProperty("port", port);
        config.addDataSourceProperty("databaseName", database);
        Map<String, String> properties = new HashMap<>();
        properties.put("useUnicode", "true");
        properties.put("characterEncoding", "utf8");
        properties.put("useSSL", "false");
        properties.put("verifyServerCertificate", "false");
        properties.put("autoReconnect", "true");
        properties.put("useMysqlMetadata", "false");
        String propertiesString = properties.entrySet().stream()
                .map(e -> e.getKey() + "=" + e.getValue())
                .collect(Collectors.joining(";"));
        config.addDataSourceProperty("properties", propertiesString);
        config.setUsername(username);
        config.setPassword(password);
        config.setMaxLifetime(60000L);
        config.setMinimumIdle(1);
        config.setIdleTimeout(30000L);
        config.setConnectionTimeout(10000L);
        config.setMaximumPoolSize(2);
        dataSource = new HikariDataSource(config);
    }

    public CompletableFuture<Tag> getTag(String name) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection connection = dataSource.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(SELECT_TAG_BY_NAME)) {
                preparedStatement.setString(1, name);
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        return tagFromResultSet(resultSet);
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return null;
        }, executor);
    }

    public CompletableFuture<List<Tag>> getAllTags() {
        return CompletableFuture.supplyAsync(() -> {
            List<Tag> tags = new ArrayList<>();
            try (Connection connection = dataSource.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(SELECT_ALL_TAGS); ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    Tag tag = tagFromResultSet(resultSet);
                    tags.add(tag);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return tags;
        }, executor);
    }

    public CompletableFuture<Void> updateTag(Tag tag) {
        return CompletableFuture.runAsync(() -> {
            try (Connection connection = dataSource.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(UPDATE_TAG)) {
                preparedStatement.setString(1, tag.getDisplayName());
                preparedStatement.setString(2, tag.getPermission());
                preparedStatement.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }, executor);
    }

    public CompletableFuture<Void> saveTag(Tag tag) {
        return CompletableFuture.runAsync(() -> {
            try (Connection connection = dataSource.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(INSERT_TAG)) {
                preparedStatement.setString(1, tag.getName());
                preparedStatement.setString(2, tag.getDisplayName());
                preparedStatement.setString(3, tag.getPermission());
                if (tag.getDesigner() != null) {
                    preparedStatement.setInt(4, tag.getDesigner().getId());
                } else {
                    preparedStatement.setNull(4, Types.INTEGER);
                }
                preparedStatement.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }, executor);
    }

    public CompletableFuture<Boolean> setTag(UUID uniqueId, Tag tag) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection connection = dataSource.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(SET_PLAYER_TAG)) {
                preparedStatement.setString(1, uniqueId.toString());
                preparedStatement.setString(2, tag.getName());
                preparedStatement.setString(3, tag.getName());
                return preparedStatement.executeUpdate() > 0;
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return false;
        }, executor);
    }

    public CompletableFuture<Tag> getTag(UUID uniqueId) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection connection = dataSource.getConnection(); PreparedStatement preparedStatement = connection.prepareStatement(SELECT_SELECTED_TAG)) {
                preparedStatement.setString(1, uniqueId.toString());
                try (ResultSet resultSet = preparedStatement.executeQuery()) {
                    if (resultSet.next()) {
                        return tagFromResultSet(resultSet);
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return null;
        }, executor);
    }

    public CompletableFuture<Set<Tag>> getLoadedTags(UUID uniqueId) {
        //return tagsMap.get(uniqueId);
        // TODO: check
        return null;
    }

    @Override
    public void close() throws IOException {
        dataSource.close();
    }

    private Tag tagFromResultSet(ResultSet resultSet) throws SQLException {
        TagDesigner designer = null;
        if (resultSet.getInt("designer_id") != 0) {
            designer = designerFromResultSet(resultSet);
        }
        List<String> lore = new ArrayList<>();
        String loreString = resultSet.getString("lore");
        if (!loreString.trim().isEmpty()) {
            Collections.addAll(lore, loreString.split("\n"));
        }
        return new Tag(resultSet.getString("name"), resultSet.getString("displayName"), lore, resultSet.getString("permission"), designer);
    }

    private TagDesigner designerFromResultSet(ResultSet resultSet) throws SQLException {
        return new TagDesigner(resultSet.getInt("designer_id"), resultSet.getString("username"), UUID.fromString(resultSet.getString("uniqueId")));
    }
}
