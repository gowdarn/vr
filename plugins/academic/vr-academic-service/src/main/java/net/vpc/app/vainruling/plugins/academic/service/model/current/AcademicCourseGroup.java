/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.vainruling.plugins.academic.service.model.current;

import net.vpc.common.strings.StringUtils;
import net.vpc.upa.UserFieldModifier;
import net.vpc.upa.config.Entity;
import net.vpc.upa.config.Field;
import net.vpc.upa.config.Id;
import net.vpc.upa.config.Path;
import net.vpc.upa.config.Sequence;

/**
 * Unite enseignement
 *
 * @author vpc
 */
@Entity(listOrder = "name")
@Path("Education/Config")
public class AcademicCourseGroup {

    @Id
    @Sequence

    private int id;
    @Field(modifiers = {UserFieldModifier.MAIN})
    private String name;

    private AcademicCourseLevel courseLevel;

    public AcademicCourseGroup() {
    }

    public AcademicCourseGroup(int id, String name) {
        this.id = id;
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

    public AcademicCourseLevel getCourseLevel() {
        return courseLevel;
    }

    public void setCourseLevel(AcademicCourseLevel courseLevel) {
        this.courseLevel = courseLevel;
    }

    @Override
    public String toString() {
        return StringUtils.nonnull(name);
    }

}
