package com.timsmeet.hibernate4ddl.util.generator;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.cfg.Configuration;
import org.hibernate.tool.hbm2ddl.SchemaExport;


public class SchemaGenerator {
    private Configuration cfg;

    /**
     * @param args first argument is the directory to generate the dll to
     */
    public static void main(String[] args) throws Exception {
        final String packageName = args[0];
        SchemaGenerator gen = new SchemaGenerator(packageName);
        final String directory = args[1];
        Dialect dialect = Dialect.valueOf(args[2]);
        gen.generate(dialect, directory);
    }

    public SchemaGenerator(String packageName) throws Exception {
        cfg = new Configuration();
        cfg.setProperty("hibernate.hbm2ddl.auto", "create");

        for (Class<?> clazz : getClasses(packageName)) {
            cfg.addAnnotatedClass(clazz);
        }
    }

    /**
     * Utility method used to fetch Class list based on a package name.
     * @param packageName (should be the package containing your annotated beans.
     */
    private List<Class<?>> getClasses(String packageName) throws Exception {
        File directory = null;
        try {
            ClassLoader cld = getClassLoader();
            URL resource = getResource(packageName, cld);
            directory = new File(resource.getFile());
        } catch (NullPointerException ex) {
            throw new ClassNotFoundException(packageName + " (" + directory
                    + ") does not appear to be a valid package");
        }
        return collectClasses(packageName, directory);
    }

    private ClassLoader getClassLoader() throws ClassNotFoundException {
        ClassLoader cld = Thread.currentThread().getContextClassLoader();
        if (cld == null) {
            throw new ClassNotFoundException("Can't get class loader.");
        }
        return cld;
    }

    private URL getResource(String packageName, ClassLoader cld) throws ClassNotFoundException {
        String path = packageName.replace('.', '/');
        URL resource = cld.getResource(path);
        if (resource == null) {
            throw new ClassNotFoundException("No resource for " + path);
        }
        return resource;
    }

    private List<Class<?>> collectClasses(String packageName, File directory) throws ClassNotFoundException {
        List<Class<?>> classes = new ArrayList<Class<?>>();
        if (directory.exists()) {
            String[] files = directory.list();
            for (String file : files) {
                if (file.endsWith(".class")) {
                    // removes the .class extension
                    classes.add(Class.forName(packageName + '.'
                            + file.substring(0, file.length() - 6)));
                }
            }
        } else {
            throw new ClassNotFoundException(packageName
                    + " is not a valid package");
        }
        return classes;
    }

    /**
     * Method that actually creates the file.
     *
     * @param dialect to use
     */
    private void generate(Dialect dialect, String directory) {
        cfg.setProperty("hibernate.dialect", dialect.getDialectClass());
        SchemaExport export = new SchemaExport(cfg);
        export.setFormat(true);
        export.setDelimiter(";");
        export.setOutputFile(directory + "create_db_" + dialect.name().toLowerCase() + ".sql");
        export.create(true, false);
        export.setOutputFile(directory + "drop_db_" + dialect.name().toLowerCase() + ".sql");
        export.drop(true, false);
    }

    /**
     * Holds the classnames of hibernate dialects for easy reference.
     */
    private static enum Dialect {
        DB2("org.hibernate.dialect.DB2Dialect"),
        Firebird("org.hibernate.dialect.FirebirdDialect"),
        Informix("org.hibernate.dialect.InformixDialect"),
        Interbase("org.hibernate.dialect.InterbaseDialect"),
        MicrosoftSQLServer2005("org.hibernate.dialect.SQLServer2005Dialect"),
        MicrosoftSQLServer2008("org.hibernate.dialect.SQLServer2008Dialect"),
        MySQL("org.hibernate.dialect.MySQLDialect"),
        MySQLInnoDB("org.hibernate.dialect.MySQL5InnoDBDialect"),
        Oracle8i("org.hibernate.dialect.Oracle8iDialect"),
        Oracle9i("org.hibernate.dialect.Oracle9iDialect"),
        Oracle10g("org.hibernate.dialect.Oracle10gDialect"),
        PostgreSQL81("org.hibernate.dialect.PostgreSQL81Dialect"),
        PostgreSQL("org.hibernate.dialect.PostgreSQL82Dialect"),
        HSQL("org.hibernate.dialect.HSQLDialect");

        private String dialectClass;

        private Dialect(String dialectClass) {
            this.dialectClass = dialectClass;
        }

        public String getDialectClass() {
            return dialectClass;
        }
    }
}

