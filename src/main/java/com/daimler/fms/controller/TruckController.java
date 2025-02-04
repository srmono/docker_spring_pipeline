package com.daimler.fms.controller;


import com.daimler.fms.dto.TruckDTO;
import com.daimler.fms.services.TruckService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/trucks")
public class TruckController {
    @Autowired
    private TruckService truckService;

    @GetMapping
    public ResponseEntity<List<TruckDTO>> getAllTruck(){
        return ResponseEntity.ok(
                truckService.getAllTrucks()
        );
    }

    //Get Truck By Id
    @GetMapping("/{id}")
    public ResponseEntity<TruckDTO> getTruckById(
            @PathVariable Long id
    ){
        TruckDTO truck = truckService.getTruckById(id);

        return ResponseEntity.ok(truck);
    }

    //Create Truck
    @PostMapping
    public ResponseEntity<TruckDTO> createTruck(
            @RequestBody TruckDTO truckDTO
    ){
        TruckDTO createdTruck =  truckService.createTruck(truckDTO);

        return  ResponseEntity.ok(createdTruck);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TruckDTO> updateTruck(
            @PathVariable Long id,
            @RequestBody TruckDTO truckDTO
    ){
        TruckDTO updateTruck =  truckService.updateTruck(id, truckDTO);

        return  ResponseEntity.ok(updateTruck);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteTruck( @PathVariable Long id){
        truckService.deleteTruck(id);
        return ResponseEntity.ok("Truck with Id: "+ id +" Deleted Successfully");
    }

    @GetMapping("/paginated")
    public ResponseEntity<Page<TruckDTO>> getPaginatedTruck(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "2") int size,
            @RequestParam(defaultValue = "id,asc") String[] sort
    ){
        Sort.Direction direction = Sort.Direction.fromString(sort[1]);

        Pageable pageable = PageRequest.of( page, size, Sort.by(direction, sort[0]));

        Page<TruckDTO> paginatedTrucks = truckService.getTruckByPagination(pageable);

        return ResponseEntity.ok(paginatedTrucks);
    }

}
