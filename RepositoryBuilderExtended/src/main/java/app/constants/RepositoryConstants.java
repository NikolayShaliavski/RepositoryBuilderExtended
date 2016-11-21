package app.constants;

public class RepositoryConstants {

    public final static String REPOSITORY = "package %srepositories;\n" +
            "\n" +
            "import org.springframework.stereotype.Repository;\n" +
            "import org.springframework.data.jpa.repository.JpaRepository;\n" +
            "import %s.%s;\n" +
            "\n" +
            "@Repository\n" +
            "public interface %s extends JpaRepository<%s, Long> {\n" +
            "}";
    public final static String REPOSITORY_DIRECTORY_NAME = "repositories";
    public final static String REPOSITORY_POSTFIX = "Repository";
}
