package tschipp.carryon.config;

public class BuiltConfig extends BuiltCategory {

    public final String fileName;

    public BuiltConfig(String fileName) {
        super(null, fileName);
        this.fileName = fileName;
    }
}
