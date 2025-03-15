package org.globalbioticinteractions.util;

import org.hamcrest.core.Is;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.hamcrest.MatcherAssert.assertThat;

public class DuckDBTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void loadAndQuery() throws IOException, SQLException {
        try (Connection conn = DriverManager.getConnection("jdbc:duckdb:")) {
            // create a table
            Statement stmt = conn.createStatement();
            stmt.execute("CREATE TABLE foos (foo VARCHAR, bar INTEGER)");
            // insert two items into the table
            stmt.execute("INSERT INTO foos VALUES ('donald', 1), ('mickey', 2)");

            try (ResultSet rs = stmt.executeQuery("SELECT * FROM foos where foo = 'mickey'")) {
                assertThat(rs.next(), Is.is(true));
                assertThat(rs.getString("foo"), Is.is("mickey"));
                assertThat(rs.getInt("bar"), Is.is(2));
            }
            stmt.close();
        }

    }


}