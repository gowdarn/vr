/*
 * To change this license header, choose License Headers in Project Properties.
 *
 * and open the template in the editor.
 */
package net.vpc.app.vainruling.core.service.model;

import net.vpc.upa.config.*;
import net.vpc.upa.types.DateTime;

/**
 * @author taha.bensalah@gmail.com
 */
@Entity(listOrder = "name desc")
@Path("Admin/Config")
public class AppPeriod {

    @Id
    @Sequence
    private int id;
    @Main
    @Unique
    private String name;
    @Summary
    private boolean navigatable = true;
    @Summary
    private boolean readOnly = false;
    @Summary
    private String snapshotName;
    private DateTime creationTime;

    public AppPeriod() {
    }

    public AppPeriod(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSnapshotName() {
        return snapshotName;
    }

    public void setSnapshotName(String snapshotName) {
        this.snapshotName = snapshotName;
    }

    public DateTime getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(DateTime creationTime) {
        this.creationTime = creationTime;
    }

    @Override
    public String toString() {
        return String.valueOf(name);
    }

    public boolean isNavigatable() {
        return navigatable;
    }

    public void setNavigatable(boolean navigatable) {
        this.navigatable = navigatable;
    }

    public boolean isReadOnly() {
        return readOnly;
    }

    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }
}
