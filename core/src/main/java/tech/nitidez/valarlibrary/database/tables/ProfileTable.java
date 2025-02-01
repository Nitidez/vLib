package tech.nitidez.valarlibrary.database.tables;

import java.util.Optional;
import java.util.function.Supplier;

import tech.nitidez.valarlibrary.database.data.DataTable;

public class ProfileTable extends DataTable {
    public ProfileTable() {
        super(
            "vlDB_Profile",
            new DataColumn(
                "uuid",
                String.class,
                true, false, true,
                Optional.of(36),
                Optional.empty()
            ),
            new DataColumn(
                "secondarycoins",
                String.class,
                false, false, false,
                Optional.empty(),
                Optional.of("{}")
            ),
            new DataColumn(
                "role",
                String.class,
                false, false, false,
                Optional.empty(),
                Optional.empty()
            ),
            new DataColumn(
                "faked",
                String.class,
                false, false, false,
                Optional.empty(),
                Optional.empty()
            ),
            new DataColumn(
                "tagged",
                String.class,
                false, false, false,
                Optional.empty(),
                Optional.empty()
            ),
            new DataColumn(
                "deliveries",
                String.class,
                false, false, false,
                Optional.empty(),
                Optional.of("{}")
            ),
            new DataColumn(
                "preferences",
                String.class,
                false, false, false,
                Optional.empty(),
                Optional.of("{}")
            ),
            new DataColumn(
                "titles",
                String.class,
                false, false, false,
                Optional.empty(),
                Optional.of("[]")
            ),
            new DataColumn(
                "medals",
                String.class,
                false, false, false,
                Optional.empty(),
                Optional.of("[]")
            ),
            new DataColumn(
                "achievements",
                String.class,
                false, false, false,
                Optional.empty(),
                Optional.of("[]")
            ),
            new DataColumn(
                "boosters",
                String.class,
                false, false, false,
                Optional.empty(),
                Optional.of("{}")
            ),
            new DataColumn(
                "selected",
                String.class,
                false, false, false,
                Optional.empty(),
                Optional.of("{}")
            ),
            new DataColumn(
                "created",
                Long.class,
                false, false, false,
                Optional.empty(),
                Optional.of((Supplier<Long>) () -> System.currentTimeMillis())
            ),
            new DataColumn(
                "lastlogin",
                Long.class,
                false, false, false,
                Optional.empty(),
                Optional.of((Supplier<Long>) () -> System.currentTimeMillis())
            )
        );
    }
}
