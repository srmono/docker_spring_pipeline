package com.daimler.fms.services;

import com.daimler.fms.dto.TruckDTO;
import com.daimler.fms.entity.Truck;
import com.daimler.fms.exception.TruckNotFoundException;
import com.daimler.fms.repository.TruckRepository;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TruckService {

    @Autowired
    private TruckRepository truckRepository;

    public List<TruckDTO> getAllTrucks(){
        return truckRepository.findAll()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());

    }

    //GetTruckByID
    public TruckDTO getTruckById(Long id){
       Truck truck =  truckRepository.findById(id).orElseThrow(
               () -> new TruckNotFoundException("Truck not found with ID :" + id)
       );

       return mapToDTO(truck);

    }
    //CreateTruck
    public TruckDTO createTruck(TruckDTO truckDTO){
        Truck truck = new Truck();
        truck.setModel(truckDTO.getModel());
        truck.setStatus(Truck.Status.valueOf(truckDTO.getStatus().toUpperCase()));
        truck.setDetails(truckDTO.getDetails());

        Truck saveTruck = truckRepository.save(truck);

        return mapToDTO(saveTruck);
    }
    private TruckDTO mapToDTO(Truck truck){
        return new TruckDTO(
               truck.getId(),
               truck.getModel(),
                truck.getStatus().name(),
                truck.getDetails()
        );
    }

    //UpdateTruck
    public TruckDTO updateTruck(Long id, TruckDTO truckDTO){
        Truck truck =  truckRepository.findById(id).orElseThrow(
                () -> new TruckNotFoundException("Truck not found with ID to update :" + id)
        );

        truck.setModel(truckDTO.getModel());
        truck.setStatus(Truck.Status.valueOf(truckDTO.getStatus().toUpperCase()));
        truck.setDetails(truckDTO.getDetails());

        Truck updatedTruck = truckRepository.save(truck);

        return mapToDTO(updatedTruck);
    }
    //Delete Truck
    public void deleteTruck(Long id){
        if(!truckRepository.existsById(id)){
            throw new TruckNotFoundException("Truck not found with ID to Delete :" + id);
        }

        truckRepository.deleteById(id);
    }

    //Pagination
    public Page<TruckDTO> getTruckByPagination(Pageable pageable){
        Page<Truck> truckPage = truckRepository.findAll( pageable);

        return truckPage.map(this::mapToDTO);
    }

}













