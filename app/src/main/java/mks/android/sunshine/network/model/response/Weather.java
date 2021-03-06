package mks.android.sunshine.network.model.response;

/**
 * Created by Mahesh on 17/3/16.
 */
public class Weather {

    private String id;
    private String icon;
    private String description;
    private String main;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getMain() {
        return main;
    }

    public void setMain(String main) {
        this.main = main;
    }

    @Override
    public String toString() {
        return "Class Weather : [id = " + id + ", icon = " + icon + ", description = " + description + ", main = " + main + "]";
    }
}
