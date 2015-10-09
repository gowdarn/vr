/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.vainruling.plugins.equipments.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.vpc.app.vainruling.api.AppPlugin;
import net.vpc.app.vainruling.api.CorePlugin;
import net.vpc.app.vainruling.api.Install;
import net.vpc.app.vainruling.api.InstallDemo;
import net.vpc.app.vainruling.api.VrApp;
import net.vpc.app.vainruling.api.model.AppContact;
import net.vpc.app.vainruling.plugins.commonmodel.service.model.AppArea;
import net.vpc.app.vainruling.plugins.commonmodel.service.model.AppAreaType;
import net.vpc.app.vainruling.api.model.AppProfile;
import net.vpc.app.vainruling.api.model.AppUser;
import net.vpc.app.vainruling.api.model.AppUserType;
import net.vpc.app.vainruling.plugins.equipments.service.model.Equipment;
import net.vpc.app.vainruling.plugins.equipments.service.model.EquipmentBrand;
import net.vpc.app.vainruling.plugins.equipments.service.model.EquipmentBrandLine;
import net.vpc.app.vainruling.plugins.equipments.service.model.EquipmentProperty;
import net.vpc.app.vainruling.plugins.equipments.service.model.EquipmentStatusType;
import net.vpc.app.vainruling.plugins.equipments.service.model.EquipmentType;
import net.vpc.app.vainruling.plugins.equipments.service.model.EquipmentTypeGroup;
import net.vpc.common.utils.Utils;
import net.vpc.upa.PersistenceUnit;
import net.vpc.upa.UPA;
import org.springframework.beans.factory.annotation.Autowired;

/**
 *
 * @author vpc
 */
@AppPlugin(version = "1.1", dependsOn = "commonModel")
public class EquipmentPlugin {

    @Autowired
    CorePlugin core;

    public Equipment findEquipment(int id) {
        PersistenceUnit pu = UPA.getPersistenceUnit();
        return (Equipment) pu.findById(Equipment.class, id);
    }

    public List<Equipment> findEquipments() {
        PersistenceUnit pu = UPA.getPersistenceUnit();
        return pu.createQuery("Select a from Equipment a order by a.name")
                .getEntityList();

    }

    public List<Equipment> findEquipmentsByType(int typeId) {
        PersistenceUnit pu = UPA.getPersistenceUnit();
        return pu.createQuery("Select a from Equipment a where a.typeId=:typeId order by a.name")
                .setParameter("typeId", typeId)
                .getEntityList();

    }

    //TODO
    public List<Equipment> findEquipmentsByArea(int typeId, int areaId, boolean deep) {
        PersistenceUnit pu = UPA.getPersistenceUnit();
        return pu.createQuery("Select a from Equipment a where a.typeId=:typeId order by a.name")
                .setParameter("typeId", typeId)
                .setParameter("areaId", areaId)
                .setParameter("deep", deep)
                .getEntityList();

    }

    @Install
    public void installService() {
        CorePlugin core = VrApp.getBean(CorePlugin.class);

        AppUserType technicianType;
        technicianType = new AppUserType();
        technicianType.setName("Technician");
        technicianType = core.findOrCreate(technicianType);

        AppProfile technicianProfile;
        technicianProfile = new AppProfile();
        technicianProfile.setName("Technician");
        technicianProfile = core.findOrCreate(technicianProfile);

        AppProfile headOfDepartment;
        headOfDepartment = new AppProfile();
        headOfDepartment.setName(CorePlugin.PROFILE_HEAD_OF_DEPARTMENT);
        headOfDepartment = core.findOrCreate(headOfDepartment);

//        core.profileAddRight(headOfDepartment.getId(),"Custom.Education.MyCourseLoad");
        for (net.vpc.upa.Entity ee : UPA.getPersistenceUnit().getPackage("Equipment").getEntities(true)) {
            core.addProfileRight(headOfDepartment.getId(), ee.getAbsoluteName() + ".Persist");
            core.addProfileRight(headOfDepartment.getId(), ee.getAbsoluteName() + ".Remove");
            core.addProfileRight(headOfDepartment.getId(), ee.getAbsoluteName() + ".Update");
            core.addProfileRight(headOfDepartment.getId(), ee.getAbsoluteName() + ".Navigate");
            core.addProfileRight(headOfDepartment.getId(), ee.getAbsoluteName() + ".Load");
            core.addProfileRight(headOfDepartment.getId(), ee.getAbsoluteName() + ".DefaultEditor");

            core.addProfileRight(technicianProfile.getId(), ee.getAbsoluteName() + ".Persist");
            core.addProfileRight(technicianProfile.getId(), ee.getAbsoluteName() + ".Update");
            core.addProfileRight(technicianProfile.getId(), ee.getAbsoluteName() + ".Navigate");
            core.addProfileRight(technicianProfile.getId(), ee.getAbsoluteName() + ".Load");
            core.addProfileRight(technicianProfile.getId(), ee.getAbsoluteName() + ".DefaultEditor");
        }

        AppContact techContact = new AppContact();
        AppUser tech1 = new AppUser();
        tech1.setEnabled(true);
        techContact.setFirstName("riadh");
        techContact.setLastName("tech");
        techContact.setFullName("riadh");
        tech1.setLogin("riadh");
        tech1.setPassword("riadh");
        techContact.setCivility(core.findCivility("M."));
        techContact.setEmail("riadh@vr.net");
        techContact.setGender(core.findGender("H"));
        tech1.setType(technicianType);
        techContact = core.findOrCreateContact(techContact);
        tech1.setContact(techContact);
        tech1 = core.findOrCreate(tech1);

        techContact = new AppContact();
        AppUser tech2 = new AppUser();
        tech2.setEnabled(true);
        techContact.setFirstName("sameh");
        techContact.setLastName("tech");
        techContact.setFullName("techsameh");
        tech2.setLogin("sameh");
        tech2.setPassword("sameh");
        techContact.setCivility(core.findCivility("Mme"));
        techContact.setEmail("sameh@vr.net");
        techContact.setGender(core.findGender("F"));
        tech2.setType(technicianType);
        techContact = core.findOrCreateContact(techContact);
        tech2.setContact(techContact);
        tech2 = core.findOrCreate(tech2);

        core.userAddProfile(tech1.getId(), "Technician");
        core.userAddProfile(tech2.getId(), "Technician");
    }

    private static class InitData {

        AppAreaType areaType_etablissement = new AppAreaType("etablissement");
        AppAreaType areaType_bloc = new AppAreaType("bloc");
        AppAreaType areaType_salle = new AppAreaType("salle");
        AppAreaType areaType_armoire = new AppAreaType("armoire");
        AppAreaType areaType_rangement = new AppAreaType("rangement");
    }

    @InstallDemo
    public void installDemoService() {
        PersistenceUnit pu = UPA.getPersistenceUnit();
        InitData initData = new InitData();
        initData.areaType_etablissement = core.findOrCreate(initData.areaType_etablissement);
        initData.areaType_bloc = core.findOrCreate(initData.areaType_bloc);
        initData.areaType_salle = core.findOrCreate(initData.areaType_salle);
        initData.areaType_armoire = core.findOrCreate(initData.areaType_armoire);
        initData.areaType_rangement = core.findOrCreate(initData.areaType_rangement);

        List<AppArea> areas = new ArrayList<>();
        List<AppArea> salles = new ArrayList<>();
        AppArea areaEniso = new AppArea("Eniso", null, initData.areaType_etablissement, null);
        areaEniso = core.findOrCreate(areaEniso);
        areas.add(areaEniso);
        AppArea areaBlocA = new AppArea("Bloc A", null, initData.areaType_bloc, areaEniso);
        areaBlocA = core.findOrCreate(areaBlocA);
        areas.add(areaBlocA);
        AppArea areaBlocB = new AppArea("Bloc B", null, initData.areaType_bloc, areaEniso);
        areaBlocB = core.findOrCreate(areaBlocB);
        areas.add(areaBlocB);

        AppArea areaBlocII = new AppArea("Bloc II", null, initData.areaType_bloc, areaEniso);
        areaBlocII = core.findOrCreate(areaBlocII);
        areas.add(areaBlocII);
        for (String s : new String[]{"II01", "II02", "II03", "II12", "II13", "II21", "II22"}) {
            AppArea salle = new AppArea(s, null, initData.areaType_salle, areaBlocII);
            salle = core.findOrCreate(salle);
            areas.add(salle);
            salles.add(salle);
        }

//        List<EquipmentBrand> brands = new ArrayList<>();
//        List<EquipmentBrandLine> brandLines = new ArrayList<>();
//        List<EquipmentType> eqTypes = new ArrayList<>();
//        List<EquipmentTypeGroup> eqTypesGroups = new ArrayList<>();
        Map<String, Object> cached = new HashMap<String, Object>();

        for (String n : new String[]{
            "Materiel Info/PC Desktop/HP/Pavillon/Pavillon 123",
            "Materiel Info/PC Portable/HP/Pavillon/Pavillon 456",
            "Materiel Info/PC Desktop/TOSHIBA/Satellite/Sat 222",
            "Materiel Info/PC Portable/TOSHIBA/Satellite/Expatria",
            "Materiel Info/Imprimante/CANON/LBP/LBP2900",
            "Materiel Info/Imprimante/CANON/LBP/LBP2900B",
            "Materiel Info/Scanner/CANON/ScanLite/ScanLite200",
            "Materiel Info/PC Portable/IBM/Thinkpad/T200",}) {
            String[] nn = n.split("/");
            String eqTypeGroupName = nn[0];
            String eqTypeName = nn[1];
            String eqBrandName = nn[2];
            String eqLineName = nn[3];
            String eqName = nn[4];

            EquipmentTypeGroup eqTypeGroup = (EquipmentTypeGroup) cached.get(eqTypeGroupName);
            if (eqTypeGroup == null) {
                eqTypeGroup = new EquipmentTypeGroup();
                eqTypeGroup.setName(eqTypeGroupName);
                eqTypeGroup = core.findOrCreate(eqTypeGroup);
                cached.put(eqTypeGroupName, eqTypeGroup);
            }

            EquipmentType eqType = (EquipmentType) cached.get(eqTypeName);
            if (eqType == null) {
                eqType = new EquipmentType();
                eqType.setTypeGroup(eqTypeGroup);
                eqType.setName(eqTypeName);
                eqType = core.findOrCreate(eqType);
                cached.put(eqTypeName, eqType);
            }

            EquipmentBrand eqBrand = (EquipmentBrand) cached.get(eqBrandName);
            if (eqBrand == null) {
                eqBrand = new EquipmentBrand();
                eqBrand.setName(eqBrandName);
                eqBrand = core.findOrCreate(eqBrand);
                cached.put(eqBrandName, eqBrand);
            }

            EquipmentBrandLine eqBrandLine = (EquipmentBrandLine) cached.get(eqLineName);
            if (eqBrandLine == null) {
                eqBrandLine = new EquipmentBrandLine();
                eqBrandLine.setBrand(eqBrand);
                eqBrandLine.setName(eqLineName);
                eqBrandLine = core.findOrCreate(eqBrandLine);
                cached.put(eqLineName, eqBrandLine);
            }

            Equipment e = new Equipment();
            e.setName(eqName);
            e.setSerial(UUID.randomUUID().toString());
            e.setType(eqType);
            e.setBrandLine(eqBrandLine);
            e.setStatusType(Utils.rand(EquipmentStatusType.class));
            e.setLocation(Utils.rand(salles));
            e = core.findOrCreate(e);
            int count = pu.createQueryBuilder(EquipmentProperty.class).setExpression("equipmentId=" + e.getId()).getEntityList().size();
            if (count == 0) {
                for (String nnn : new String[]{"Color", "Width", "Height"}) {
                    EquipmentProperty x = new EquipmentProperty();
                    x.setName(nnn);
                    x.setValue("100");
                    x.setEquipment(e);
                    pu.persist(x);
                }
            }
        }
    }

}
