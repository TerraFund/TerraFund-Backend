package com.example.TerraFund.repositories;

import com.example.TerraFund.entities.Message;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageRepository extends JpaRepository <Message, Long>{

}
