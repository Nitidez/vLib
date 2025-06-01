package tech.nitidez.valarlibrary.data.tables;

import java.util.Optional;

import tech.nitidez.valarlibrary.data.data.DataTable;

public class SkinsTable extends DataTable {
    public SkinsTable() {
        super(
            "vlDB_Skins",
            new DataColumn(
                "id",
                Integer.class,
                true, true, false, Optional.empty(), Optional.empty()
            ),
            new DataColumn(
                "owner",
                String.class,
                false, false, false, Optional.empty(), Optional.empty()
            )
        );
    }
}
