/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.vainruling.api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import net.vpc.app.vainruling.api.security.UserSession;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import net.vpc.app.vainruling.api.model.AppCivility;
import net.vpc.app.vainruling.api.model.AppCompany;
import net.vpc.app.vainruling.api.model.AppConfig;
import net.vpc.app.vainruling.api.model.AppContact;
import net.vpc.app.vainruling.api.model.AppCountry;
import net.vpc.app.vainruling.api.model.AppDepartment;
import net.vpc.app.vainruling.api.model.AppGender;
import net.vpc.app.vainruling.api.model.AppIndustry;
import net.vpc.app.vainruling.api.model.AppPeriod;
import net.vpc.app.vainruling.api.model.AppUser;
import net.vpc.app.vainruling.api.model.AppProfile;
import net.vpc.app.vainruling.api.model.AppProfileRight;
import net.vpc.app.vainruling.api.model.AppProperty;
import net.vpc.app.vainruling.api.model.AppUserProfileBinding;
import net.vpc.app.vainruling.api.model.AppRightName;
import net.vpc.app.vainruling.api.model.AppUserType;
import net.vpc.app.vainruling.api.util.InSetEvaluator;
import net.vpc.common.strings.StringUtils;
import net.vpc.upa.Entity;
import net.vpc.upa.EntityShield;
import net.vpc.upa.PersistenceUnit;
import net.vpc.upa.QueryBuilder;
import net.vpc.upa.UPA;
import net.vpc.upa.UserPrincipal;
import net.vpc.upa.expressions.Equals;
import net.vpc.upa.expressions.Literal;
import net.vpc.upa.expressions.Var;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

/**
 *
 * @author vpc
 */
@AppPlugin(version = "1.8")
public class CorePlugin {

    private static final java.util.logging.Logger log = java.util.logging.Logger.getLogger(CorePlugin.class.getName());

    public static final String USER_ADMIN = "admin";
    public static final String PROFILE_ADMIN = "Admin";
    public static final String PROFILE_HEAD_OF_DEPARTMENT = "HeadOfDepartment";
    public static final Set<String> ADMIN_ENTITIES = Collections.unmodifiableSet(new HashSet<>(Arrays.asList("Trace", "User", "UserProfile", "UserProfileBinding", "UserProfileRight")));
    @Autowired
    private TraceService trace;
    private boolean updatingPoll = false;

    public void onPoll() {
        if (!updatingPoll) {
            updatingPoll = true;
            try {
                synchronized (this) {
                    ApplicationContext context = VrApp.getContext();
                    for (String pn : context.getBeanNamesForType(PollAware.class)) {
                        PollAware b = (PollAware) context.getBean(pn);
                        b.onPoll();
                    }
                }
            } finally {
                updatingPoll = false;
            }
        }
    }

//    private void init(){
//    }
    public UserSession getUserSession() {
        return VrApp.getContext().getBean(UserSession.class);
    }

    public AppUser findUser(String login) {
        PersistenceUnit pu = UPA.getPersistenceUnit();
        return (AppUser) pu.findByField(AppUser.class, "login", login);
    }

    public AppUser findUser(int id) {
        PersistenceUnit pu = UPA.getPersistenceUnit();
        return (AppUser) pu.findById(AppUser.class, id);
    }

    public AppUserType findUserType(String name) {
        PersistenceUnit pu = UPA.getPersistenceUnit();
        return (AppUserType) pu.findByMainField(AppUserType.class, name);
    }

    public AppUserType findUserType(int id) {
        PersistenceUnit pu = UPA.getPersistenceUnit();
        return (AppUserType) pu.findById(AppUserType.class, id);
    }

    public AppProfile findProfile(int profileId) {
        PersistenceUnit pu = UPA.getPersistenceUnit();
        return (AppProfile) pu.findById(AppProfile.class, profileId);
    }

    public boolean createRight(String rightName, String desc) {
        PersistenceUnit pu = UPA.getPersistenceUnit();
        AppRightName r = pu.findById(AppRightName.class, rightName);
        if (r != null) {
            return false;
        }
        r = new AppRightName();
        r.setName(rightName);
        r.setDescription(desc);
        pu.persist(r);
        return true;
    }

    public List<AppRightName>[] findProfileRightNamesDualList(int profileId) {
        PersistenceUnit pu = UPA.getPersistenceUnit();
        Map<String, AppRightName> allMap = new HashMap<>();
        Map<String, AppRightName> existing = new HashMap<>();
        List<AppRightName> oldRigths = pu.createQuery("Select u.`right` from AppProfileRight u where u.profileId=:profileId")
                .setParameter("profileId", profileId)
                .getEntityList();
        List<AppRightName> allRigths = pu.createQuery("Select u from AppRightName u")
                .getEntityList();
        for (AppRightName r : allRigths) {
            allMap.put(r.getName(), r);
        }
        for (AppRightName r : oldRigths) {
            existing.put(r.getName(), r);
            allMap.remove(r.getName());
        }
        List<AppRightName> in = new ArrayList<>(existing.values());
        List<AppRightName> out = new ArrayList<>(allMap.values());
        return new List[]{in, out};
    }

    public List<AppUser>[] findProfileUsersDualList(int profileId) {
        PersistenceUnit pu = UPA.getPersistenceUnit();
        Map<String, AppUser> allMap = new HashMap<>();
        Map<String, AppUser> existing = new HashMap<>();
        List<AppUser> oldRigths = pu.createQuery("Select u.user from AppUserProfileBinding u where u.profileId=:profileId")
                .setParameter("profileId", profileId)
                .getEntityList();
        List<AppUser> allRigths = pu.createQuery("Select u from AppUser u")
                .getEntityList();
        for (AppUser r : allRigths) {
            allMap.put(r.getLogin(), r);
        }
        for (AppUser r : oldRigths) {
            existing.put(r.getLogin(), r);
            allMap.remove(r.getLogin());
        }
        List<AppUser> in = new ArrayList<>(existing.values());
        List<AppUser> out = new ArrayList<>(allMap.values());
        return new List[]{in, out};
    }

    public int setProfileRights(int profileId, List<String> rightNames) {
        PersistenceUnit pu = UPA.getPersistenceUnit();
        AppProfile p = pu.findById(AppProfile.class, profileId);
        if (p == null) {
            throw new IllegalArgumentException("Profile not found " + profileId);
        }
        List<AppProfileRight> oldRigths = pu.createQuery("Select u from AppProfileRight u where u.profileId=:profileId")
                .setParameter("profileId", profileId)
                .getEntityList();
        Set<String> baseSet = new HashSet<String>();
        Set<String> visitedSet = new HashSet<String>();
        if (rightNames != null) {
            baseSet.addAll(rightNames);
        }
        int modifications = 0;
        for (AppProfileRight r : oldRigths) {
            if (baseSet.contains(r.getRight().getName())) {
                //ok
            } else {
                pu.remove(r);
                modifications++;
            }
            visitedSet.add(r.getRight().getName());
        }
        for (String s : baseSet) {
            if (!visitedSet.contains(s)) {
                //this is new
                AppRightName r = pu.findById(AppRightName.class, s);
                if (r == null) {
                    log.log(Level.SEVERE, "Right " + s + " not found");
                } else {
                    AppProfileRight pr = new AppProfileRight();
                    pr.setProfile(p);
                    pr.setRight(r);
                    pu.persist(pr);
                    modifications++;
                }
            }
        }
        return modifications;
    }

    public int setProfileUsers(int profileId, List<String> logins) {
        PersistenceUnit pu = UPA.getPersistenceUnit();
        AppProfile p = pu.findById(AppProfile.class, profileId);
        if (p == null) {
            throw new IllegalArgumentException("Profile not found " + profileId);
        }
        List<AppUserProfileBinding> oldUserBindings = pu.createQuery("Select u from AppUserProfileBinding u where u.profileId=:profileId")
                .setParameter("profileId", profileId)
                .getEntityList();
        List<AppUser> oldUsers = pu.createQuery("Select u from AppUser u")
                .getEntityList();
        Map<String, AppUser> usersByName = new HashMap<String, AppUser>();
        for (AppUser u : oldUsers) {
            usersByName.put(u.getLogin(), u);
        }
        Set<String> baseSet = new HashSet<String>();
        Set<String> visitedSet = new HashSet<String>();
        if (logins != null) {
            baseSet.addAll(logins);
        }
        int modifications = 0;
        for (AppUserProfileBinding r : oldUserBindings) {
            if (baseSet.contains(r.getUser().getLogin())) {
                //ok
            } else {
                pu.remove(r);
                modifications++;
            }
            visitedSet.add(r.getUser().getLogin());
        }
        for (String s : baseSet) {
            if (!visitedSet.contains(s)) {
                //this is new
                AppUser r = usersByName.get(s);
                if (r == null) {
                    log.log(Level.SEVERE, "User " + s + " not found");
                } else {
                    AppUserProfileBinding pr = new AppUserProfileBinding();
                    pr.setProfile(p);
                    pr.setUser(r);
                    pu.persist(pr);
                    modifications++;
                }
            }
        }
        return modifications;
    }

    public boolean addProfileRight(int profileId, String rightName) {
        PersistenceUnit pu = UPA.getPersistenceUnit();
        AppProfile p = pu.findById(AppProfile.class, profileId);
        if (p == null) {
            throw new IllegalArgumentException("Profile not found " + profileId);
        }
        AppRightName r = pu.findById(AppRightName.class, rightName);
        if (r == null) {
            log.log(Level.SEVERE, "Right " + rightName + " not found");
            return false;
        }
        AppProfileRight pr = pu.createQuery("Select u from AppProfileRight u where u.profileId=:profileId and u.rightName=:rightName")
                .setParameter("rightName", rightName)
                .setParameter("profileId", profileId)
                .getEntity();
        if (pr == null) {
            pr = new AppProfileRight();
            pr.setProfile(p);
            pr.setRight(r);
            pu.persist(pr);
            return true;
        }
        return false;
    }

    public boolean userRemoveProfile(int userId, int profileId) {
        PersistenceUnit pu = UPA.getPersistenceUnit();
        AppUserProfileBinding old = pu.createQuery("Select u from AppUserProfileBinding  u where u.userId=:userId and u.profileId=:profileId")
                .setParameter("userId", userId)
                .setParameter("profileId", profileId)
                .getEntity();
        if (old != null) {
            pu.remove(old);
            return true;
        }
        return false;
    }

    public boolean userAddProfile(int userId, String profileCode) {
        PersistenceUnit pu = UPA.getPersistenceUnit();
        if (pu.createQuery("Select u.profile from AppUserProfileBinding  u where u.userId=:userId and u.profile.name=:name")
                .setParameter("userId", userId)
                .setParameter("name", profileCode)
                .isEmpty()) {
            AppUser u = findUser(userId);
            if (u == null) {
                throw new IllegalArgumentException("Unknown User " + userId);
            }
            AppProfile p = findProfileByCode(profileCode);
            if (p == null) {
                p = findProfileByName(profileCode);
            }
            if (p == null) {
                p = new AppProfile();
                p.setName(profileCode);
                p.setCode(profileCode);
                pu.persist(p);
            }

            pu.persist(new AppUserProfileBinding(u, p));
            return true;
        }
        return false;
    }

    public boolean userAddProfile(int userId, int profileId) {
        PersistenceUnit pu = UPA.getPersistenceUnit();
        AppUserProfileBinding b = pu.createQuery("Select u from AppUserProfileBinding  u where u.userId=:userId and u.profileId=:profileId")
                .setParameter("userId", userId)
                .setParameter("profileId", profileId)
                .getEntity();
        if (b == null) {
            AppUser u = findUser(userId);
            if (u == null) {
                throw new IllegalArgumentException("Unknown User " + userId);
            }
            AppProfile p = findProfile(profileId);
            if (p == null) {
                throw new IllegalArgumentException("Unknown Profile " + profileId);
            }
            pu.persist(new AppUserProfileBinding(u, p));
            return true;
        }
        return false;
    }

    public boolean userHasProfile(int userId, String profileName) {
        PersistenceUnit pu = UPA.getPersistenceUnit();
        return !pu.createQuery("Select u.profile from AppUserProfileBinding  u where u.userId=:userId and u.profile.name=:name")
                .setParameter("userId", userId)
                .setParameter("name", profileName)
                .isEmpty();
    }

    public Set<String> findUserRights(int userId) {
        PersistenceUnit pu = UPA.getPersistenceUnit();
        List<String> rights = pu.createQuery("Select n.rightName from AppUserProfileBinding  u "
                + " inner join AppProfileRight n on n.profileId=u.profileId "
                + " where u.userId=:userId")
                .setParameter("userId", userId)
                .getValueList(0);
        return new HashSet<>(rights);
    }

    public AppProfile findOrCreateCustomProfile(String profileCode, String customType) {
        AppProfile p = findProfileByCode(profileCode);
        if (p == null) {
            p = findProfileByName(profileCode);
        }
        if (p == null) {
            p = new AppProfile();
            p.setName(profileCode);
            p.setCode(profileCode);
            p.setCustom(true);
            p.setCustomType(customType);
            UPA.getPersistenceUnit().persist(p);
        } else if (!p.isCustom()) {
            //force to custom
            p.setCustom(true);
            p.setCustomType(customType);
            UPA.getPersistenceUnit().merge(p);
        }
        if (StringUtils.isEmpty(p.getCode())) {
            p.setCode(p.getName());
            UPA.getPersistenceUnit().merge(p);
        }
        if (StringUtils.isEmpty(p.getName())) {
            p.setCode(p.getCode());
            UPA.getPersistenceUnit().merge(p);
        }
        return p;
    }

    public AppProfile findOrCreateProfile(String profileName) {
        AppProfile p = findProfileByName(profileName);
        if (p == null) {
            p = new AppProfile();
            p.setName(profileName);
            UPA.getPersistenceUnit().persist(p);
        }
        return p;
    }

    public AppProfile findProfileByName(String profileName) {
        PersistenceUnit pu = UPA.getPersistenceUnit();
        return pu.createQueryBuilder(AppProfile.class).addAndField("name", profileName)
                .getEntity();

    }

    public AppProfile findProfileByCode(String profileCode) {
        PersistenceUnit pu = UPA.getPersistenceUnit();
        return pu.createQueryBuilder(AppProfile.class).addAndField("code", profileCode)
                .getEntity();

    }

    public List<AppProfile> findProfilesByUser(int userId) {
        PersistenceUnit pu = UPA.getPersistenceUnit();
        return pu.createQuery("Select u.profile from AppUserProfileBinding  u where u.userId=:userId")
                .setParameter("userId", userId)
                .getEntityList();
    }

    public List<AppUser> findUsersByProfile(int profileId) {
        PersistenceUnit pu = UPA.getPersistenceUnit();
        return pu.createQuery("Select u.user from AppUserProfileBinding  u where u.profileId=:profileId")
                .setParameter("profileId", profileId)
                .getEntityList();
    }

    public List<AppUser> findUsersByProfile(String profileName) {
        PersistenceUnit pu = UPA.getPersistenceUnit();
        return pu.createQuery("Select u.user from AppUserProfileBinding  u where u.profile.name=:profileName")
                .setParameter("profileName", profileName)
                .getEntityList();
    }

    public List<AppProfile> findProfiles() {
        PersistenceUnit pu = UPA.getPersistenceUnit();
        return pu.createQuery("Select u from AppProfile u")
                .getEntityList();
    }

    public List<AppUser> findUsers() {
        PersistenceUnit pu = UPA.getPersistenceUnit();
        return pu.createQuery("Select u from AppUser  u")
                .getEntityList();
    }

    public List<AppUser> findEnabledUsers() {
        PersistenceUnit pu = UPA.getPersistenceUnit();
        return pu.createQuery("Select u from AppUser  u where u.enabled=true and u.deleted=false")
                .getEntityList();
    }

    public AppUser findUser(String login, String password) {
        PersistenceUnit pu = UPA.getPersistenceUnit();
        return (AppUser) pu
                .createQuery("Select u from AppUser u "
                        + "where "
                        + "u.login=:login "
                        + "and u.password=:password")
                .setParameter("login", login)
                .setParameter("password", password)
                .getEntity();
    }

    public AppDepartment findDepartment(int id) {
        return UPA.getPersistenceUnit().findById(AppDepartment.class, id);
    }

    public List<AppDepartment> findDepartments() {
        return UPA.getPersistenceUnit().findAll(AppDepartment.class);
    }

    public AppDepartment findDepartment(String code) {
        return UPA.getPersistenceUnit().
                createQuery("Select a from AppDepartment a where a.code=:code or a.name=:code or a.name2=:code")
                .setParameter("code", code)
                .getEntity();
    }

    public AppCivility findCivility(String t) {
        return (AppCivility) UPA.getPersistenceUnit().findByMainField(AppCivility.class, t);
    }

    public List<AppCivility> findCivilities() {
        return UPA.getPersistenceUnit().findAll(AppCivility.class);
    }

    public AppContact findContact(int id) {
        return (AppContact) UPA.getPersistenceUnit().findById(AppContact.class, id);
    }

    public AppCompany findCompany(int id) {
        PersistenceUnit pu = UPA.getPersistenceUnit();
        return (AppCompany) pu.createQuery("Select u from AppCompany u where u.id=:id").setParameter("id", id)
                .setHint("navigationDepth", 3)
                .getEntity();
//        return (AppCompany) pu.findById(AppCompany.class, id);
    }

    public AppCompany findCompany(String name) {
        PersistenceUnit pu = UPA.getPersistenceUnit();
        return (AppCompany) pu.createQuery("Select u from AppCompany u where u.name=:name").setParameter("name", name)
                .setHint("navigationDepth", 3)
                .getEntity();
//
//        return (AppCompany) UPA.getPersistenceUnit().findByMainField(AppCompany.class, name);
    }

    public List<AppCompany> findCompanies() {
        return UPA.getPersistenceUnit().findAll(AppCompany.class);
    }

    public AppGender findGender(String t) {
        return (AppGender) UPA.getPersistenceUnit().findByMainField(AppGender.class, t);
    }

    public List<AppGender> findGenders() {
        List<AppGender> all = UPA.getPersistenceUnit().findAll(AppGender.class);
        return all;
    }

    private static class InitData {

        long now;
        AppProfile adminProfile;
        AppUser admin;
        AppUserType adminType;
        List<AppCivility> civilities;
        List<AppGender> genders;
        List<AppDepartment> departments;
    }

    @Install
    public void installService() {
        PersistenceUnit pu = UPA.getPersistenceUnit();
        AppCountry tunisia = pu.findByMainField(AppCountry.class, "Tunisie");
        if (tunisia == null) {
            tunisia = new AppCountry();
            tunisia.setName("Tunisie");
            pu.persist(tunisia);
        }
        AppIndustry eduIndustry = pu.findByMainField(AppIndustry.class, "Education");
        if (eduIndustry == null) {
            eduIndustry = new AppIndustry();
            eduIndustry.setName("Education");
            pu.persist(eduIndustry);
        }

        AppCompany eniso = pu.findByMainField(AppIndustry.class, "ENISo");
        if (eniso == null) {
            eniso = new AppCompany();
            eniso.setName("ENISo");
            eniso.setIndustry(eduIndustry);
            eniso.setCountry(tunisia);
            pu.persist(eniso);
        }

        AppProfile oldAdmin = pu.findByMainField(AppProfile.class, "admin");
        if (oldAdmin != null) {
            oldAdmin.setName("Admin");
            pu.merge(oldAdmin);
        }
        InitData d = new InitData();
        d.now = System.currentTimeMillis();
        d.adminProfile = new AppProfile();
        d.adminProfile.setName("Admin");
        d.adminProfile = findOrCreate(d.adminProfile);
        d.adminProfile.setCustom(true);
        d.adminProfile.setCustomType("Profile");
        pu.merge(d.adminProfile);

        d.adminType = new AppUserType();
        d.adminType.setName("Admin");
        d.adminType = findOrCreate(d.adminType);

        d.civilities = new ArrayList<>();
        for (String n : new String[]{"M.", "Mlle", "Mme"}) {
            AppCivility c = new AppCivility(0, n);
            c = findOrCreate(c);
            d.civilities.add(c);
        }
        d.genders = new ArrayList<>();
        for (String n : new String[]{"H", "F"}) {
            AppGender c = new AppGender(0, n);
            c = findOrCreate(c);
            d.genders.add(c);
        }
        AppContact adminContact = new AppContact();
        adminContact.setFirstName("admin");
        adminContact.setLastName("admin");
        adminContact.setFullName("admin");
        adminContact.setCivility(d.civilities.get(0));
        adminContact.setGender(d.genders.get(0));
        adminContact.setEmail("admin@vr.net");
        adminContact = findOrCreate(adminContact, "firstName");

        AppUser uu = new AppUser();
        d.admin = uu;
        d.admin.setLogin("admin");
        d.admin.setPassword("admin");
        d.admin.setType(d.adminType);
        d.admin.setContact(adminContact);
        d.admin.setEnabled(true);
        d.admin = findOrCreate(d.admin);
        if (d.admin == uu) {
            pu.persist(new AppUserProfileBinding(d.admin, d.adminProfile));
        }
        d.departments = new ArrayList<>();
        for (String[] n : new String[][]{{"II", "Informatique Industrielle"}, {"EI", "Electronique Indstrielle"}, {"MA", "Mecanique Avancee"}, {"ADM", "Administration"}}) {
            AppDepartment c = new AppDepartment();
            c.setCode(n[0]);
            c.setName(n[1]);
            AppDepartment old = pu.findByField(AppDepartment.class, "code", c.getCode());
            if (old == null) {
                c = findOrCreate(c);
            } else {
                c = old;
            }
            d.departments.add(c);
        }
        AppConfig mainConfig = new AppConfig();
        mainConfig.setId(1);
        mainConfig.setMainCompany(eniso);
        mainConfig.setMainPeriod(new AppPeriod("2015-2016"));
        findOrCreate(mainConfig, "id");
        validateRightsDefinitions();
    }

    @Start
    protected void validateRightsDefinitions() {
        PersistenceUnit pu = UPA.getPersistenceUnit();
        for (Entity entity : pu.getEntities()) {
            if (!entity.isSystem()) {
                EntityShield s = entity.getShield();
                if (true) {
                    AppRightName r = new AppRightName();
                    r.setName(entity.getAbsoluteName() + ".DefaultEditor");
                    r.setDescription("List " + entity.getName());
                    findOrCreate(r);
                }
                if (true) {
                    AppRightName r = new AppRightName();
                    r.setName(entity.getAbsoluteName() + ".Navigate");
                    r.setDescription("List " + entity.getName());
                    findOrCreate(r);
                }
                if (true) {
                    AppRightName r = new AppRightName();
                    r.setName(entity.getAbsoluteName() + ".Load");
                    r.setDescription("Detail " + entity.getName());
                    findOrCreate(r);
                }
                if (!ADMIN_ENTITIES.contains(entity.getName())) {
                    if (s.isPersistSupported()) {
                        AppRightName r = new AppRightName();
                        r.setName(entity.getAbsoluteName() + ".Persist");
                        r.setDescription("Persist " + entity.getName());
                        findOrCreate(r);
                    }
                    if (s.isUpdateSupported()) {
                        AppRightName r = new AppRightName();
                        r.setName(entity.getAbsoluteName() + ".Update");
                        r.setDescription("Update " + entity.getName());
                        findOrCreate(r);
                    }
                    if (s.isDeleteSupported()) {
                        AppRightName r = new AppRightName();
                        r.setName(entity.getAbsoluteName() + ".Remove");
                        r.setDescription("Remove " + entity.getName());
                        findOrCreate(r);
                    }
                    String extraActions = entity.getProperties().getString("actions");
                    if (extraActions != null) {
                        for (String a : extraActions.split(" ,|;")) {
                            if (a.length() > 0) {
                                AppRightName r = new AppRightName();
                                r.setName(entity.getAbsoluteName() + "." + a);
                                r.setDescription(a + " " + entity.getName());
                                findOrCreate(r);
                            }
                        }
                    }
                }
            }
        }
        createRight("Custom.Admin.Passwd", "Custom.Admin.Passwd");
    }

    public <T> T findOrCreate(T o) {
        PersistenceUnit pu = UPA.getPersistenceUnit();
        Entity e = pu.getEntity(o.getClass());
        return findOrCreate(o, e.getMainField().getName());
    }

    public <T> T findOrCreate(T o, String field) {
        PersistenceUnit pu = UPA.getPersistenceUnit();
        Entity e = pu.getEntity(o.getClass());
        Object value = e.getBuilder().objectToRecord(o, true).getObject(field);
        T t = pu.createQueryBuilder(o.getClass()).setExpression(new Equals(new Var(field), new Literal(value, e.getField(field).getDataType())))
                .getEntity();
        if (t == null) {
            pu.persist(o);
            return o;
        }
        return t;
    }

    public <T> List<T> filterByProfilePattern(List<T> in, Integer userId, String login, ProfilePatternFilter<T> filter) {
        List<T> out = new ArrayList<>();
        final HashMap<String, Object> cache = new HashMap<String, Object>();
        if (in != null) {
            for (T i : in) {
                if (userMatchesProfileFilter(userId, login, new ProfileFilterExpression(filter.getProfilePattern(i)), cache)) {
                    out.add(i);
                }
            }
        }
        return out;
    }

    private InSetEvaluator createProfilesEvaluator(final Set<String> profiles) {
        return new InSetEvaluator(profiles);
    }

    public boolean userMatchesProfileFilter(int userId, String profileExpr) {
        return userMatchesProfileFilter(userId, null, profileExpr, null);
    }

    public boolean userMatchesProfileFilter(String userLogin, String profileExpr) {
        return userMatchesProfileFilter(null, userLogin, profileExpr, null);
    }

    public boolean userMatchesProfileFilter(Integer userId, String login, String profile, String whereClause) {
        return userMatchesProfileFilter(userId, login, profile, whereClause, null);
    }

    private boolean userMatchesProfileFilter(Integer userId, String login, String profile, String whereClause, Map<String, Object> cache) {
        return userMatchesProfileFilter(userId, login, new ProfileFilterExpression(profile, whereClause), cache);
    }

    private boolean userMatchesProfileFilter(Integer userId, String login, ProfileFilterExpression profileExpr, Map<String, Object> cache) {
        if (StringUtils.isEmpty(profileExpr.getFilterExpression()) && StringUtils.isEmpty(profileExpr.getProfileListExpression())) {
            return true;
        }
        if (cache == null) {
            cache = new HashMap<>();
        }
        PersistenceUnit pu = UPA.getPersistenceUnit();
        AppUser u = null;
        if (userId != null) {
            Map<Integer, AppUser> usersById = (Map<Integer, AppUser>) cache.get("usersById");
            if (usersById == null) {
                usersById = new HashMap<>();
                cache.put("usersById", usersById);
            }
            u = usersById.get(userId);
            if (u == null) {
                u = findUser(userId);
                if (u != null) {
                    usersById.put(userId, u);
                }
            }
        }
        if (!StringUtils.isEmpty(login)) {
            Map<String, AppUser> usersByLogin = (Map<String, AppUser>) cache.get("usersByLogin");
            if (usersByLogin == null) {
                usersByLogin = new HashMap<>();
                cache.put("usersByLogin", usersByLogin);
            }

            AppUser u1 = null;
            u1 = usersByLogin.get(login);
            if (u1 == null) {
                u1 = findUser(login);
                if (u1 != null) {
                    usersByLogin.put(login, u1);
                }
            }
            if (u != null && u1 != null && u.getId() != u1.getId()) {
                u = null;
            } else if (u == null) {
                u = u1;
            }
        }

        Map<Integer, Set<String>> usersProfilesByUserId = (Map<Integer, Set<String>>) cache.get("usersProfilesByUserId");
        if (usersProfilesByUserId == null) {
            usersProfilesByUserId = new HashMap<>();
            cache.put("usersProfilesByUserId", usersProfilesByUserId);
        }
        Set<String> foundProfileNames = null;
        if (u != null) {
            foundProfileNames = (Set<String>) usersProfilesByUserId.get(u.getId());
            if (foundProfileNames == null) {
                foundProfileNames = new HashSet<>();
                for (AppProfile p : findProfilesByUser(u.getId())) {
                    foundProfileNames.add(p.getName().toLowerCase());
                }
                foundProfileNames.add(u.getLogin().toLowerCase());
                usersProfilesByUserId.put(u.getId(), foundProfileNames);
            }
        } else {
            foundProfileNames = new HashSet<>();
        }
        InSetEvaluator evaluator = createProfilesEvaluator(foundProfileNames);
        boolean b = evaluator.evaluate(profileExpr.getProfileListExpression());
        if (b && !StringUtils.isEmpty(profileExpr.getFilterExpression())) {
            return filterUsersByExpression(Arrays.asList(u), profileExpr.getFilterExpression()).size() > 0;
        }
        return b;
    }

//    public List<AppProfile> resolveProfilesByProfileFilter(String profile) {
//        PersistenceUnit pu = UPA.getPersistenceUnit();
//        if (profile != null && profile.trim().length() > 0) {
//            StringBuilder x = new StringBuilder();
//            for (String p : profile.split(" , |;")) {
//                if (p != null) {
//                    x.append("/").append(p);
//                }
//            }
//            x.append("/");
//            return pu.createQuery("Select u from AppProfile u where :expr like concat('%/',u.name,'/%')")
//                    .setParameter("expr", x.toString())
//                    .getEntityList();
//        }
//        return Collections.EMPTY_LIST;
//    }
    public List<AppUser> findUsersByProfileFilter(String profilePattern) {
        //check if pattern contains where clause!
        ProfileFilterExpression ee = new ProfileFilterExpression(profilePattern);
        ProfileFilterExpression profilesOnlyExpr = new ProfileFilterExpression(ee.getProfileListExpression(), null);

        List<AppUser> all = new ArrayList<>();
        final HashMap<String, Object> cache = new HashMap<String, Object>();
        List<AppUser> users = findEnabledUsers();
        HashMap<Integer, AppUser> usersById = new HashMap<Integer, AppUser>();
        HashMap<String, AppUser> usersByLogin = new HashMap<String, AppUser>();
        for (AppUser user : users) {
            usersById.put(user.getId(), user);
            usersByLogin.put(user.getLogin(), user);
        }
        cache.put("usersById", usersById);
        cache.put("usersByLogin", usersByLogin);
        for (AppUser u : users) {
            if (userMatchesProfileFilter(u.getId(), u.getLogin(), profilesOnlyExpr, cache)) {
                all.add(u);
            }
        }
        return filterUsersByExpression(all, ee.getFilterExpression());
    }

    private List<AppUser> filterUsersByExpression(List<AppUser> all, String expression) {
        if (StringUtils.isEmpty(expression)) {
            return new ArrayList<AppUser>(all);
        }
        HashSet<Integer> ids = new HashSet<>();
        for (AppUser i : all) {
            ids.add(i.getId());
        }
        return UPA.getPersistenceUnit()
                .createQuery("Select x from AppUser x where " + expression)
                .getEntityList();
    }
//    public List<AppUser> resolveUsersByProfileFilter(String profile) {
//        PersistenceUnit pu = UPA.getPersistenceUnit();
//        if (profile != null && profile.trim().length() > 0) {
//            StringBuilder x = new StringBuilder();
//            for (String p : profile.split(" , |;")) {
//                if (p != null) {
//                    x.append("/").append(p);
//                }
//            }
//            return pu.createQuery("Select u.user from AppUserProfileBinding u where :expr like concat('/',u.profile.name,'/')")
//                    .setParameter("expr", x.toString())
//                    .getEntityList();
//        }
//        return Collections.EMPTY_LIST;
//    }

    public void setAppProperty(String propertyName, String userLogin, Object propertyValue) {
        PersistenceUnit pu = UPA.getPersistenceUnit();
        AppUser u = null;
        if (userLogin != null) {
            u = findUser(userLogin);
        }
        AppProperty ap = new AppProperty();
        ap.setEnabled(true);
        ap.setUser(u);
        ap.setPropertyName(propertyName);
        String propertyType = "null";
        String propertyValueString = "";
        if (propertyValue != null) {
            if (propertyValue instanceof String) {
                propertyValueString = String.valueOf(propertyValue);
                propertyType = "string";
            } else if (propertyValue instanceof Integer) {
                propertyValueString = String.valueOf(propertyValue);
                propertyType = "int";
            } else if (propertyValue instanceof Long) {
                propertyValueString = String.valueOf(propertyValue);
                propertyType = "long";
            } else if (propertyValue instanceof Double) {
                propertyValueString = String.valueOf(propertyValue);
                propertyType = "double";
            } else if (propertyValue instanceof Boolean) {
                propertyValueString = String.valueOf(propertyValue);
                propertyType = "boolean";
            } else {
                throw new IllegalArgumentException("Not supported " + propertyValue);
            }
        }
        ap.setPropertyType(propertyType);
        ap.setPropertyValue(propertyValueString);
        setAppProperty(ap);
    }

    public void setEnabledAppProperty(String propertyName, String userLogin, boolean enabled) {
        PersistenceUnit pu = UPA.getPersistenceUnit();
        AppUser u = null;
        if (userLogin != null) {
            u = findUser(userLogin);
        }
        AppProperty ap = new AppProperty();
        ap.setEnabled(true);
        ap.setUser(u);
        ap.setPropertyName(propertyName);
        String propertyType = "null";
        String propertyValueString = "";
        ap.setPropertyType(propertyType);
        ap.setPropertyValue(propertyValueString);
        ap.setEnabled(enabled);
        setAppProperty(ap);
    }

    public Object getAppPropertyValue(AppProperty p) {
        if (p == null) {
            return null;
        }
        String t = p.getPropertyType();
        String v = p.getPropertyValue();
        if ("null".equalsIgnoreCase(t)) {
            return null;
        }
        if ("string".equalsIgnoreCase(t)) {
            return v;
        }
        if ("int".equalsIgnoreCase(t)) {
            if (v.isEmpty()) {
                return null;
            }
            return Integer.valueOf(v);
        }
        if ("long".equalsIgnoreCase(t)) {
            if (v.isEmpty()) {
                return null;
            }
            return Long.valueOf(v);
        }
        if ("double".equalsIgnoreCase(t)) {
            if (v.isEmpty()) {
                return null;
            }
            return Double.valueOf(v);
        }
        if ("boolean".equalsIgnoreCase(t)) {
            if (v.isEmpty()) {
                return null;
            }
            return Boolean.valueOf(v);
        }
        throw new IllegalArgumentException("Unsupported");
    }

    public Object getOrCreateAppPropertyValue(String propertyName, String userLogin, Object value) {
        AppProperty p = getAppProperty(propertyName, userLogin);
        if (p != null) {
            if (p.isEnabled()) {
                Object v = getAppPropertyValue(p);
                if (v != null) {
                    return v;
                }
            }
        }
        if (value != null) {
            setAppProperty(propertyName, userLogin, value);
        }
        return value;
    }

    public Object getAppPropertyValue(String propertyName, String userLogin) {
        AppProperty p = getAppProperty(propertyName, userLogin);
        if (p != null && p.isEnabled()) {
            return getAppPropertyValue(p);
        }
        return null;
    }

    public AppProperty getAppProperty(String propertyName, String userLogin) {

        PersistenceUnit pu = UPA.getPersistenceUnit();
        AppUser u = null;
        if (userLogin != null) {
            u = findUser(userLogin);
            if (u == null) {
                return null;
            }
        }

        QueryBuilder q = pu.createQueryBuilder(AppProperty.class);
        q.addAndField("propertyName", propertyName);
        if (u != null) {
            q.addAndExpression("(userId=" + u.getId() + " or userId = null)");
        } else {
            q.addAndExpression("(userId = null)");
        }
        List<AppProperty> props = q.getEntityList();
        List<AppProperty> all = new ArrayList<AppProperty>(props);
        Collections.sort(all, new Comparator<AppProperty>() {

            @Override
            public int compare(AppProperty o1, AppProperty o2) {
                AppUser u1 = o1.getUser();
                AppUser u2 = o2.getUser();
                if (u1 == null && u2 != null) {
                    return 1;

                } else if (u1 != null && u2 == null) {
                    return -1;
                }
                String s1 = o1.getPropertyName();
                String s2 = o2.getPropertyName();
                if (s1 == null) {
                    s1 = "";
                }
                if (s2 == null) {
                    s2 = "";
                }
                int x = s1.compareTo(s2);
                if (x != 0) {
                    return x;
                }
                x = s1.compareTo(s2);
                if (x != 0) {
                    return x;
                }
                return 0;
            }
        });
        if (all.size() > 0) {
            return all.get(0);
        }
        return null;
    }

    public void setAppProperty(AppProperty ap) {
        PersistenceUnit pu = UPA.getPersistenceUnit();
        AppProperty old = pu.createQueryBuilder(AppProperty.class)
                .addAndField("propertyName", ap.getPropertyName())
                .getEntity();
        if (old == null) {
            pu.persist(ap);
        } else {
            old.setEnabled(ap.isEnabled());
            old.setPropertyType(ap.getPropertyType());
            old.setPropertyValue(ap.getPropertyValue());
            pu.merge(old);
        }
    }

    public AppUser findUserByContact(int contactId) {
        PersistenceUnit pu = UPA.getPersistenceUnit();
        return pu.createQueryBuilder(AppUser.class)
                .addAndField("contactId", contactId)
                .getEntity();
    }

    public AppContact findOrCreateContact(AppContact c) {
        PersistenceUnit pu = UPA.getPersistenceUnit();
        if (!StringUtils.isEmpty(c.getNin())) {
            AppContact oldAcademicTeacher = pu.createQueryBuilder(AppContact.class)
                    .addAndField("nin", c.getNin())
                    .getEntity();
            if (oldAcademicTeacher != null) {
                return oldAcademicTeacher;
            }
        } else {
            AppContact oldAcademicTeacher = pu.createQueryBuilder(AppContact.class)
                    .addAndField("firstName", c.getFirstName())
                    .addAndField("lastName", c.getLastName())
                    .getEntity();
            if (oldAcademicTeacher != null) {
                return oldAcademicTeacher;
            }
        }
        pu.persist(c);
        return c;
    }

    public String getActualLogin() {
        UserPrincipal up = UPA.getPersistenceUnit().getUserPrincipal();
        if (up != null && up.getObject() instanceof AppUser) {
            AppUser u = (AppUser) up.getObject();
            return u.getLogin();
        }
        UserSession us = VrApp.getBean(UserSession.class);
        if (us != null) {
            if (us.getUser() != null) {
                return us.getUser().getLogin();
            }
        }
        return null;
    }

    public boolean isActualAdminOrUser(String login) {
        UserSession us = VrApp.getBean(UserSession.class);
        UserPrincipal up = UPA.getPersistenceUnit().getUserPrincipal();
        if (up != null && up.getObject() instanceof AppUser) {
            AppUser u = (AppUser) up;
            if (us.getUser() != null) {
                String login2 = u.getLogin();
                if (login2.equals(login)) {
                    return true;
                }
                if (login2.equals(us.getUser().getLogin())) {
                    return us.isAdmin();
                }
            }
            CorePlugin core = VrApp.getBean(CorePlugin.class);
            List<AppProfile> profiles = core.findProfilesByUser(u.getId());
            for (AppProfile p : profiles) {
                if (PROFILE_ADMIN.equals(p.getCode())) {
                    return true;
                }
            }
            return false;
        }
        if (us != null) {
            if (us.isAdmin()) {
                return true;
            }
            if (us.getUser().getLogin().equals(login)) {
                return true;
            }
        }
        return us != null && us.isAdmin();
    }

    public boolean isActualAdmin() {
        UserSession us = null;
        try {
            us = VrApp.getBean(UserSession.class);
        } catch (Exception e) {
            //session not yet created!
            return true;
        }
        UserPrincipal up = UPA.getPersistenceUnit().getUserPrincipal();
        if (up != null) {
            if (up.getName().equals("<internal>")) {
                return true;
            }
            if (up.getObject() instanceof AppUser) {
                AppUser u = (AppUser) up.getObject();
                if (us.getUser() != null && u.getLogin().equals(us.getUser().getLogin())) {
                    return us.isAdmin();
                }
                CorePlugin core = VrApp.getBean(CorePlugin.class);
                List<AppProfile> profiles = core.findProfilesByUser(u.getId());
                for (AppProfile p : profiles) {
                    if (PROFILE_ADMIN.equals(p.getCode())) {
                        return true;
                    }
                }
            }
        }
        return us != null && us.isAdmin();
    }

    public boolean isSessionAdmin() {
        UserSession us = VrApp.getBean(UserSession.class);
        return us != null && us.isAdmin();
    }

    public String validateName(String text) {
        //make it kamel based
        boolean wasWhite = true;
        char[] chars = (text == null ? "" : text).toCharArray();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < chars.length; i++) {
            char aChar = chars[i];
            if (Character.isWhitespace(aChar)) {
                if (!(aChar == ' ' || aChar == '\t')) {
                    aChar = ' ';
                }
                if (!wasWhite) {
                    sb.append(aChar);
                }
                wasWhite = true;
            } else if (wasWhite) {
                sb.append(Character.toUpperCase(aChar));
                wasWhite = false;
            } else {
                sb.append(Character.toLowerCase(aChar));
                wasWhite = false;
            }
        }
        return sb.toString().trim();
    }

    public String resolvePasswordProposal(AppContact contact) {
        String fn = contact.getFirstName();
        String ln = contact.getLastName();
        if (fn == null) {
            fn = "";
        }
        if (ln == null) {
            ln = "";
        }
        String fnlower = fn.toLowerCase();
        String[] fns = fnlower.split(" ");
        if (fns.length > 0 && fns[0].length() >= 3) {
            String p = fns[0];
            for (int i = 0; i < 4; i++) {
                int x = (int) (Math.random() * 10);
                p += x;
            }
            return p;
        }
//        if()
        String p = fnlower.replace(" ", "");
        for (int i = 0; i < 4; i++) {
            int x = (int) (Math.random() * 10);
            p += x;
        }
        return p;
    }

    public String resolveLoginProposal(AppContact contact) {
        String fn = contact.getFirstName();
        String ln = contact.getLastName();
        if (fn == null) {
            fn = "";
        }
        if (ln == null) {
            ln = "";
        }
        return fn.toLowerCase().replace(" ", "") + "." + ln.toLowerCase().replace(" ", "");
    }

    public void runThread(Runnable r) {
        new SpringThread(r).start();
    }

    public AppConfig findAppConfig() {
        PersistenceUnit pu = UPA.getPersistenceUnit();
        AppConfig c = pu.findById(AppConfig.class, 1);
        //should exist;
        return c;
    }

    public AppUser createUser(AppContact contact, int userTypeId, int departmentId, boolean attachToExistingUser, String[] defaultProfiles) {
        AppUser u = findUserByContact(contact.getId());
        if (u == null) {
            String login = resolveLoginProposal(contact);
            if (StringUtils.isEmpty(login)) {
                login = "user";
            }
            String password = resolvePasswordProposal(contact);
            u = findUser(login);
            if (u != null && u.getContact() != null) {
                if (u.getContact().getId() == contact.getId()) {
                    //this is the same user !! ok
                    return u;
                }
            }
            if (!attachToExistingUser || (u != null && u.getContact() != null)) {
                String y = String.valueOf(Calendar.getInstance().get(Calendar.YEAR) - 2000);
                if (u != null) {
                    u = findUser(login + y);
                    if (u == null) {
                        //ok
                        login = login + y;
                    } else {
                        String chars = "abcdefghijklmnopqrstuvwxyz";
                        for (int i = 0; i < chars.length(); i++) {
                            u = findUser(login + y + chars.charAt(i));
                            if (u == null) {
                                login = login + y + chars.charAt(i);
                                break;
                            }
                        }
                    }
                }
                if (u != null) {
                    int index = 1;
                    while (true) {
                        u = findUser(login + y + "_" + index);
                        if (u == null) {
                            login = login + y + "_" + index;
                            break;
                        }
                        index++;
                    }
                }
                if (u != null) {
                    throw new IllegalArgumentException("Unable to add new user");
                }
            }
            AppUserType userType = findUserType(userTypeId);
            AppDepartment userDepatment = findDepartment(departmentId);
            if (u == null) {
                u = new AppUser();
                u.setLogin(login);
                u.setContact(contact);
                String pwd = password;
                u.setPassword(pwd);
                u.setPasswordAuto(pwd);
                u.setType(userType);
                u.setDepartment(userDepatment);
                u.setEnabled(true);
                UPA.getPersistenceUnit().persist(u);
            } else {
                u.setContact(contact);
                u.setType(userType);
                UPA.getPersistenceUnit().merge(u);
            }
        }
        if (defaultProfiles != null) {
            for (String defaultProfile : defaultProfiles) {
                if (!StringUtils.isEmpty(defaultProfile)) {
                    userAddProfile(u.getId(), defaultProfile);
                }
            }
        }
        return u;
    }

    public boolean isComplexProfileExpr(String s) {
        for (char c : s.toCharArray()) {
            switch (c) {
                case '(':
                case ')':
                case '[':
                case ']':
                case '+':
                case '-':
                case '_':
                case ':':
                case ',':
                case ';':
                case ' ':
                case '&':
                case '|': {
                    return true;
                }
                default: {
                    break;
                }
            }
        }
        return false;
    }

    public String validateProfileName(String s) {
        StringBuilder sb = new StringBuilder();
        for (char c : s.toCharArray()) {
            switch (c) {
                case '(':
                case ')':
                case '[':
                case ']':
                case '+':
                case '-':
                case '_':
                case ':':
                case ',':
                case ';':
                case ' ':
                case '&':
                case '|': {
                    sb.append("_");
                    break;
                }
                default: {
                    sb.append(c);
                }
            }
        }
        return sb.toString();
    }

    public void passwd(String login, String oldPassword, String newPassword) {
        if (newPassword == null) {
            newPassword = "";
        }
        if (newPassword.trim().length() == 0) {
            throw new RuntimeException("Mot de passe trop court");
        }
        if (newPassword.length() == 5) {
            throw new RuntimeException("Mot de passe trop court");
        }
        if (StringUtils.isEmpty(login)) {
            login = getActualLogin();
        }
        if (isActualAdmin()) {
            AppUser u = findUser(login);
            if (u == null) {
                throw new RuntimeException("User not found " + login);
            }

            u.setPassword(newPassword);
            UPA.getPersistenceUnit().merge(u);
        } else {
            AppUser u = findUser(login, oldPassword);
            if (u == null) {
                throw new RuntimeException("Invalid User or Password for " + login);
            }
            u.setPassword(newPassword);
            UPA.getPersistenceUnit().merge(u);
        }
    }
}