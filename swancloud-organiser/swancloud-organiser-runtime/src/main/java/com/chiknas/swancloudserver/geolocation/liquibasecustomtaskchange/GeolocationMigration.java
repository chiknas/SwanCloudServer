package com.chiknas.swancloudserver.geolocation.liquibasecustomtaskchange;

import com.chiknas.swancloudserver.services.helpers.FilesHelper;
import com.drew.lang.GeoLocation;
import liquibase.change.custom.CustomTaskChange;
import liquibase.database.Database;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.CustomChangeException;
import liquibase.exception.SetupException;
import liquibase.exception.ValidationErrors;
import liquibase.resource.ResourceAccessor;

import java.nio.file.Path;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * Extracts longitude/latitude information from all the files in file_metadata table.
 * Creates the new geolocation entity when possible and populates the table.
 */
public class GeolocationMigration implements CustomTaskChange {

    private static final String SELECT_FILES = "SELECT * FROM file_metadata";

    private static final String INSERT_GEOLOCATION = "INSERT INTO geolocation(longitude, latitude) VALUES (?, ?)";
    private static final String FILE_METADATA_GEOLOCATION_UPDATE = "UPDATE file_metadata SET geolocation_id=? WHERE id=?";

    @Override
    public void execute(Database database) throws CustomChangeException {
        JdbcConnection connection = (JdbcConnection) database.getConnection();
        ResultSet resultSet = getAllFileMetadata(connection);

        try {
            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String path = resultSet.getString("path");

                FilesHelper.getGeolocation(Path.of(path).toFile())
                        .map(geolocation -> insertGeolocation(connection, geolocation))
                        .ifPresent(geolocationId -> updateGeolocationFileMetadataEntry(connection, id, geolocationId));
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to get file metadata results.", e);
        }
    }

    private static ResultSet getAllFileMetadata(JdbcConnection connection) {
        try {
            PreparedStatement statement = connection.prepareStatement(SELECT_FILES);
            return statement.executeQuery();
        } catch (Exception e) {
            throw new RuntimeException("Failed to load all file metadata entries.", e);
        }
    }

    private static void updateGeolocationFileMetadataEntry(JdbcConnection connection, int id, int geolocationId) {
        try {
            PreparedStatement updateFileMetadataGeolocationStatement = connection.prepareStatement(FILE_METADATA_GEOLOCATION_UPDATE);
            updateFileMetadataGeolocationStatement.setInt(1, geolocationId);
            updateFileMetadataGeolocationStatement.setInt(2, id);
            updateFileMetadataGeolocationStatement.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("Failed to update geolocation for file metadata with id: " + id, e);
        }
    }

    private static int insertGeolocation(JdbcConnection connection, GeoLocation geolocation) {
        try {
            PreparedStatement insertGeolocationStatement = connection.prepareStatement(INSERT_GEOLOCATION, Statement.RETURN_GENERATED_KEYS);
            insertGeolocationStatement.setDouble(1, geolocation.getLongitude());
            insertGeolocationStatement.setDouble(2, geolocation.getLatitude());
            insertGeolocationStatement.executeUpdate();

            ResultSet generatedKeys = insertGeolocationStatement.getGeneratedKeys();
            generatedKeys.next();
            return generatedKeys.getInt("ID");
        } catch (Exception e) {
            throw new RuntimeException("Failed to insert new geolocation entry.", e);
        }
    }

    @Override
    public String getConfirmationMessage() {
        return "File Metadata geolocation migration was completed successfully!";
    }

    @Override
    public void setUp() throws SetupException {

    }

    @Override
    public void setFileOpener(ResourceAccessor resourceAccessor) {
    }

    @Override
    public ValidationErrors validate(Database database) {
        return null;
    }
}
