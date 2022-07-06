package git.hardyethan.secretnotebook;

import java.nio.file.Paths;
import git.hardyethan.secretnotebook.gui.GUI;

public class Main {

    public static void main(String[] args) {
        new GUI(Paths.get(System.getProperty("user.dir"), "database.json").toAbsolutePath().toString());
    }

}
