package com.eldoheiri.datastore.implementations.selection;

import java.sql.SQLException;
import java.util.List;

import com.eldoheiri.datastore.sqlpredicatebuilder.basetypes.buildersinterfaces.IPredicate;

public interface IDataStoreSelector {
    <Entity> List<Entity> get(IPredicate predicate, Class<Entity> entityClass) throws SQLException;
}
