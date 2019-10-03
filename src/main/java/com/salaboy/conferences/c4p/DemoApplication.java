package com.salaboy.conferences.c4p;

import com.salaboy.conferences.c4p.model.Proposal;
import com.salaboy.conferences.c4p.model.ProposalDecision;
import com.salaboy.conferences.c4p.model.ProposalStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.cloud.stream.messaging.Processor;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@SpringBootApplication
@RestController
@EnableBinding(Processor.class)
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

    @Value("${version:0}")
    private String version;

    private Set<Proposal> proposals = new HashSet<>();

    @GetMapping("/info")
    public String infoWithVersion() {
        return "C4P v" + version;
    }

    @PostMapping()
    public String newProposal(@RequestBody Proposal proposal) {
        System.out.println("> New Proposal Received: " + proposal);
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

    @PatchMapping("/{id}/rank/{rank}")
    public void rank(@PathVariable("id") String id, @PathVariable("rank") Integer rank) {

        Optional<Proposal> proposalOptional = proposals.stream().filter(p -> p.getId().equals(id)).findFirst();
        if (proposalOptional.isPresent()) {
            Proposal proposal = proposalOptional.get();
            proposal.setRank(rank);
            proposal.setStatus(ProposalStatus.RANKED);
            System.out.println(">  Proposal Ranked: " + proposal);
            proposals.add(proposal);
        }
    }

    @PatchMapping("/{id}/decision")
    public void rank(@PathVariable("id") String id, @RequestBody ProposalDecision decision) {
        Optional<Proposal> proposalOptional = proposals.stream().filter(p -> p.getId().equals(id)).findFirst();
        if (proposalOptional.isPresent()) {
            Proposal proposal = proposalOptional.get();
            proposal.setApproved(decision.isApproved());
            proposal.setStatus(ProposalStatus.DECIDED);
            System.out.println(">  Proposal Approved: " + proposal);
            proposals.add(proposal);
        }

        System.out.println("> Notify Speaker");


        if(decision.isApproved()) {
            System.out.println("> Send request to Agenda");
        }

        // Call Agenda Service
        // POST proposal data to AGENDA / for new item to be added in the agenda
    }


    @StreamListener(Processor.INPUT)
    @SendTo(Processor.OUTPUT)
    public byte[] handle(byte[] in){
        return new String(in).toUpperCase().getBytes();
    }

}
