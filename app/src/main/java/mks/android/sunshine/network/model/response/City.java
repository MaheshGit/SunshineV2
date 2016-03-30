package mks.android.sunshine.network.model.response;

/**
 * Created by Mahesh on 17/3/16.
 */
public class City {
    private Coordinate coord;
    private String id;
    private String name;
    private String population;
    private String country;

    public Coordinate getCoord() {
        return coord;
    }

    public void setCoord(Coordinate coord) {
        this.coord = coord;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPopulation() {
        return population;
    }

    public void setPopulation(String population) {
        this.population = population;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    @Override
    public String toString()
    {
        return "Class City :  [coord = "+coord+", id = "+id+", name = "+name+", population = "+population+", country = "+country+"]";
    }
}
