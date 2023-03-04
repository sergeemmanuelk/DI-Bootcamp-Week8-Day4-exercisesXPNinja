
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import db.DbConnect;
import model.Actor;

public class App {
    public static void main(String[] args) throws Exception {
        System.out.println("Hello, World!");
    }

    public void addActorAndAssignFilm(Actor actor, int filmId) {
        // Request for insert an actor into the actor table
        String sqlInsertActor = "INSERT INTO actor" +
                "(first_name, last_name)" +
                "VALUES(?, ?)";

        // Request for assign actor to a film
        final String sqlAssignActor = "INSERT INTO film_actor" +
                "(actor_id, film_id)" +
                "VALUES(?, ?)";

        Connection conn = null;
        PreparedStatement preparedStatement = null;
        PreparedStatement preparedStatement2 = null;
        ResultSet resultSet = null;

        int actorId = 0;

        try {
            // connection to database
            conn = new DbConnect().connect();
            conn.setAutoCommit(false);

            // add actor
            preparedStatement = conn.prepareStatement(sqlInsertActor, Statement.RETURN_GENERATED_KEYS);

            preparedStatement.setString(1, actor.getFirstName());
            preparedStatement.setString(2, actor.getLastName());

            int affectedRows = preparedStatement.executeUpdate();

            if (affectedRows > 0) {
                // get actor id
                resultSet = preparedStatement.getGeneratedKeys();

                if (resultSet.next()) {
                    actorId = resultSet.getInt(1);
                    if (actorId > 0) {
                        preparedStatement2 = conn.prepareStatement(sqlAssignActor);
                        preparedStatement2.setInt(1, actorId);
                        preparedStatement2.setInt(2, filmId);
                        preparedStatement2.executeUpdate();
                    }
                }
            } else {
                // rollback the transaction if the insert failed
                conn.rollback();
            }

            // commit the transaction if everything is fine
            conn.commit();

            System.out.println(String.format("The actor was inserted with id %d and " + "assigned to the film %d",
                    actorId, filmId));

        } catch (SQLException e) {
            e.printStackTrace();
            // roll back the transaction
            System.out.println("Rolling back the transaction...");
            try {
                conn.rollback();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }

        } finally {
            this.closeResources(resultSet).closeResources(preparedStatement).closeResources(preparedStatement2)
                    .closeResources(conn);
        }
    }

    /**
     * Close a AutoCloseable object
     *
     * @param closeable
     */
    private App closeResources(AutoCloseable closeable) {
        try {
            if (closeable != null) {
                closeable.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return this;
    }
}
