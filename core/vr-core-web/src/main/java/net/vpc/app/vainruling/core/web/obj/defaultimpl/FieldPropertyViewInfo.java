/*
 * To change this license header, choose License Headers in Project Properties.
 *
 * and open the template in the editor.
 */
package net.vpc.app.vainruling.core.web.obj.defaultimpl;

import net.vpc.app.vainruling.core.service.CorePlugin;
import net.vpc.app.vainruling.core.service.VrApp;
import net.vpc.app.vainruling.core.service.util.UIConstants;
import net.vpc.app.vainruling.core.web.ctrl.EditCtrlMode;
import net.vpc.app.vainruling.core.web.obj.ObjCtrl;
import net.vpc.app.vainruling.core.web.obj.UPAObjectHelper;
import net.vpc.upa.AccessLevel;
import net.vpc.upa.Field;
import net.vpc.upa.FieldModifier;
import net.vpc.upa.FlagSet;
import net.vpc.upa.types.DataType;

import java.util.Map;

/**
 * @author taha.bensalah@gmail.com
 */
public class FieldPropertyViewInfo {

    public boolean main;
    public boolean id;
    public boolean insert;
    public boolean update;
    public boolean insert_seq;
    public boolean nullable;
    public boolean listMode;
    public boolean insertMode;
    public boolean updateMode;
    public boolean visible;
    public boolean disabled;
    public DataType dataType;

    private FieldPropertyViewInfo(Field field, DataType dataType, Map<String, Object> configuration) {
        this.dataType = dataType != null ? dataType : field.getDataType();
        if (field != null) {
            FlagSet<FieldModifier> modifiers = field.getModifiers();
            main = modifiers.contains(FieldModifier.MAIN);
            id = modifiers.contains(FieldModifier.ID);
            insert = modifiers.contains(FieldModifier.PERSIST_DEFAULT);
            insert_seq = modifiers.contains(FieldModifier.PERSIST_SEQUENCE);
            update = !id && modifiers.contains(FieldModifier.UPDATE_DEFAULT)
                    //this is added to fix problem for creation date
                    && modifiers.contains(FieldModifier.PERSIST_DEFAULT);
        } else {
            insert = true;
            update = true;
            //
        }
        nullable = dataType == null ? true : dataType.isNullable();
        ObjCtrl objCtrl = VrApp.getBean(ObjCtrl.class);
        listMode = objCtrl.getModel().getMode() == EditCtrlMode.LIST;
        insertMode = objCtrl.getModel().getMode() == EditCtrlMode.NEW;
        updateMode = objCtrl.getModel().getMode() == EditCtrlMode.UPDATE;
        if (field != null) {
            visible
                    = insertMode ? UPAObjectHelper.findBooleanProperty(field, UIConstants.Form.VISIBLE_ON_CREATE, null, !insert_seq)
                    : updateMode ? UPAObjectHelper.findBooleanProperty(field, UIConstants.Form.VISIBLE_ON_UPDATE, null, !insert_seq)
                    : true;
        } else {
            visible = true;
        }
        boolean forceDisabled = configuration != null && configuration.get("disabled") != null && (Boolean.TRUE.equals(configuration.get("disabled")) || "true".equalsIgnoreCase(String.valueOf(configuration.get("disabled"))));
        boolean forceInvisible = configuration != null && configuration.get("invisible") != null && (Boolean.TRUE.equals(configuration.get("disabled")) || "true".equalsIgnoreCase(String.valueOf(configuration.get("invisible"))));
        disabled = forceDisabled;

        if (!disabled) {
            if (insertMode) {
                if (!insert) {
                    disabled = true;
                }
            }
            if (updateMode) {
                if (!update) {
                    disabled = true;
                }
            }
        }

        if (visible && forceInvisible) {
            visible = false;
        }
        boolean admin = VrApp.getBean(CorePlugin.class).isUserSessionAdmin();

        if (visible && field != null) {
            AccessLevel rl = field.getReadAccessLevel();
            if (rl == AccessLevel.PRIVATE) {
                visible = false;
            } else if (rl == AccessLevel.PROTECTED) {
                visible = admin || field.getPersistenceGroup().getSecurityManager().isAllowedRead(field);
            }
        }

        if (updateMode && field != null) {
            if (update) {
                AccessLevel u = field.getUpdateAccessLevel();
                if (u == AccessLevel.PRIVATE) {
                    update = false;
                } else if (u == AccessLevel.PROTECTED) {
                    update = admin|| field.getPersistenceGroup().getSecurityManager().isAllowedWrite(field);
                }
            }
            if (!update) {
                disabled = true;
            }
        }

        if (insertMode && field != null) {
            if (insert) {
                AccessLevel u = field.getPersistAccessLevel();
                if (u == AccessLevel.PRIVATE) {
                    insert = false;
                } else if (u == AccessLevel.PROTECTED) {
                    insert = admin|| field.getPersistenceGroup().getSecurityManager().isAllowedWrite(field);
                }
            }
            if (!insert) {
                disabled = true;
            }
        }
    }

    public static FieldPropertyViewInfo build(Field field, DataType dataType, Map<String, Object> configuration) {
        return new FieldPropertyViewInfo(field, dataType, configuration);
    }
}
