package com.capg.RechargeHub.config;

import com.capg.RechargeHub.entity.Operator;
import com.capg.RechargeHub.entity.Plan;
import com.capg.RechargeHub.repository.OperatorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class OperatorDataInitializer implements CommandLineRunner {

    @Autowired
    private OperatorRepository operatorRepository;

    @Override
    public void run(String... args) throws Exception {
        if (operatorRepository.count() > 2) {
            // If we already have more than 2 operators, we might already have initialized them
            return;
        }

        // 1. Jio (Existing or Update)
        createOperatorIfNotExists("Jio", "Prepaid", "All India", "https://logodownload.org/wp-content/uploads/2020/05/jio-logo-1.png");

        // 2. Airtel (Fixing broken logo)
        createOperatorIfNotExists("Airtel", "Prepaid", "All India", "https://logodownload.org/wp-content/uploads/2020/11/airtel-logo-1.png");

        // 3. Vodafone Idea (Vi)
        createOperatorIfNotExists("Vi", "Prepaid", "All India", "https://upload.wikimedia.org/wikipedia/commons/thumb/0/03/Vi_logo.svg/1200px-Vi_logo.svg.png");

        // 4. BSNL
        createOperatorIfNotExists("BSNL", "Prepaid", "All India", "https://upload.wikimedia.org/wikipedia/en/thumb/e/ee/BSNL_logo.svg/1200px-BSNL_logo.svg.png");
    }

    private void createOperatorIfNotExists(String name, String type, String circle, String logoUrl) {
        if (operatorRepository.findAll().stream().anyMatch(o -> o.getName().equalsIgnoreCase(name))) {
            // Update existing if logo is different or broken
            Operator existing = operatorRepository.findAll().stream()
                    .filter(o -> o.getName().equalsIgnoreCase(name))
                    .findFirst().get();
            existing.setLogoUrl(logoUrl);
            operatorRepository.save(existing);
            return;
        }

        Operator op = new Operator();
        op.setName(name);
        op.setType(type);
        op.setCircle(circle);
        op.setLogoUrl(logoUrl);

        List<Plan> plans = new ArrayList<>();
        
        // Sample Popular Plan
        Plan p1 = new Plan();
        p1.setAmount(299.0);
        p1.setValidity("28 Days");
        p1.setData("1.5 GB/Day");
        p1.setType("Popular");
        p1.setDescription("Truly Unlimited Calls + 100 SMS/Day");
        p1.setOperator(op);
        plans.add(p1);

        // Sample Data Plan
        Plan p2 = new Plan();
        p2.setAmount(58.0);
        p2.setValidity("Existing Plan");
        p2.setData("3 GB");
        p2.setType("Data");
        p2.setDescription("Data Add-on Pack");
        p2.setOperator(op);
        plans.add(p2);

        op.setPlans(plans);
        operatorRepository.save(op);
    }
}
