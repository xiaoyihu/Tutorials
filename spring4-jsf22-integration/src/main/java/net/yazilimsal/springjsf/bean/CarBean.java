package net.yazilimsal.springjsf.bean;

import net.yazilimsal.springjsf.jsf.annotation.SpringViewScoped;
import net.yazilimsal.springjsf.model.Car;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Component
@SpringViewScoped
public class CarBean implements Serializable {

    private List<Car> carList;

    private Car car;

    @PostConstruct
    public void init() {
        carList = new ArrayList<Car>();
        car = new Car();
    }

    public void add() {
        carList.add(car);
        car = new Car();
    }

    public void remove(Car car) {
        carList.remove(car);
    }

    public Car getCar() {
        return car;
    }

    public void setCar(Car car) {
        this.car = car;
    }

    public List<Car> getCarList() {
        return carList;
    }

    public void setCarList(List<Car> carList) {
        this.carList = carList;
    }

}
