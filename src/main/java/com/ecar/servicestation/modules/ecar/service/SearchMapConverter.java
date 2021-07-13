package com.ecar.servicestation.modules.ecar.service;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Getter
public class SearchMapConverter {

    private final Node root = new Node();

    @Getter
    @NoArgsConstructor
    protected class Node {
        Map<String, Node> NodeMap = new HashMap<>();
        String tResult;
        boolean isLeafNode;
    }

    public void insert(String address) {
        Node currentNode = root;
        String[] addressDetails = address.split(" ");

        for (int i = 0; i <= 1; i++) {
            String addressDetail = addressDetails[i];
            String tResult = currentNode.getTResult();

            currentNode = currentNode.getNodeMap().computeIfAbsent(addressDetail, (key) -> new Node());
            currentNode.tResult = (tResult != null ? tResult : "") + addressDetail + " ";
        }

        String emdOrRn = addressDetails[2];

        for (int i = 0; i < emdOrRn.length(); i++) {
            String current = String.valueOf(emdOrRn.charAt(i));
            String tResult = currentNode.getTResult();

            currentNode = currentNode.getNodeMap().computeIfAbsent(current, (key) -> new Node());
            currentNode.tResult = tResult + current;
        }

        currentNode.isLeafNode = true;
    }

}
