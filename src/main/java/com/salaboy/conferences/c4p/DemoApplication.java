package com.salaboy.conferences.c4p;

import com.salaboy.conferences.c4p.model.AgendaItem;
import com.salaboy.conferences.c4p.model.Proposal;
import com.salaboy.conferences.c4p.model.ProposalDecision;
import com.salaboy.conferences.c4p.model.ProposalStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@SpringBootApplication
@RestController
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

    private static final String agendaService = "http://demo-conference-agenda";

    @Value("${version:0.0.0}")
    private String version;

    private RestTemplate restTemplate = new RestTemplate();

    private Set<Proposal> proposals = new HashSet<>();

    @GetMapping("/info")
    public String infoWithVersion() {
        return "C4P v" + version;
    }

    @PostMapping()
    public String newProposal(@RequestBody Proposal proposal) {
        System.out.println("> New Proposal Received Event ( " + proposal + ")");
        proposals.add(proposal);
        return "Thanks for submitting a Proposal, the conference organizers will get in touch soon";
    }

    @GetMapping()
    public Set<Proposal> getAll() {
        return proposals;
    }

    @GetMapping("/{id}")
    public Optional<Proposal> getById(@PathVariable("id") String id) {
        return proposals.stream().filter(p -> p.getId().equals(id)).findFirst();
    }

    @PutMapping("/{id}/decision")
    public void rank(@PathVariable("id") String id, @RequestBody ProposalDecision decision) {
        emitEvent("> Proposal Approved Event ( " + ((decision.isApproved()) ? "Approved" : "Rejected") + ")");
        Optional<Proposal> proposalOptional = proposals.stream().filter(p -> p.getId().equals(id)).findFirst();
        if (proposalOptional.isPresent()) {
            Proposal proposal = proposalOptional.get();
            proposal.setApproved(decision.isApproved());
            proposal.setStatus(ProposalStatus.DECIDED);

            proposals.add(proposal);
            emitEvent("> Notify Speaker Event (via email: " + proposal.getEmail() + " -> " + ((decision.isApproved()) ? "Approved" : "Rejected") + ")");
            if (decision.isApproved()) {
                emitEvent("> Add Proposal To Agenda Event ");
                HttpEntity<AgendaItem> request = new HttpEntity<>(new AgendaItem(proposal.getTitle(), proposal.getAuthor(), new Date()));
                restTemplate.postForEntity(agendaService, request, String.class);
            }
        } else {
            emitEvent(" Proposal Not Found Event (" + id + ")");
        }


    }

    private void emitEvent(String content) {
        System.out.println(content);
    }

}
