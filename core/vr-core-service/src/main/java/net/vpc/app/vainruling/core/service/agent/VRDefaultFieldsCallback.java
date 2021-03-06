/*
 * To change this license header, choose License Headers in Project Properties.
 *
 * and open the template in the editor.
 */
package net.vpc.app.vainruling.core.service.agent;

import net.vpc.upa.AccessLevel;
import net.vpc.upa.Field;
import net.vpc.upa.callbacks.FieldEvent;
import net.vpc.upa.config.Callback;
import net.vpc.upa.config.OnPreCreate;
import net.vpc.upa.exceptions.UPAException;

/**
 * @author taha.bensalah@gmail.com
 */
@Callback
public class VRDefaultFieldsCallback {

    @OnPreCreate
    public void onPreCreateField(FieldEvent event) throws UPAException {
        Field f = event.getField();
        String name = f.getName();
        if (name.equals("deleted")
                || name.equals("deletedOn")
                || name.equals("deletedBy")
                || name.equals("archived")
                || name.equals("archivedOn")
                || name.equals("archivedBy")) {
            f.setPersistAccessLevel(AccessLevel.PRIVATE);
            f.setUpdateAccessLevel(AccessLevel.PROTECTED);
            f.setReadAccessLevel(AccessLevel.PROTECTED);
        }
    }

}
