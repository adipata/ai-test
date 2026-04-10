package com.certalert.service;

import com.certalert.dto.GroupDTO;
import com.certalert.model.Group;
import com.certalert.repository.GroupRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class GroupService {

    private final GroupRepository groupRepository;

    public GroupService(GroupRepository groupRepository) {
        this.groupRepository = groupRepository;
    }

    public GroupDTO createGroup(GroupDTO groupDTO) {
        Group group = new Group();
        group.setName(groupDTO.getName());
        group.setDescription(groupDTO.getDescription());

        Group savedGroup = groupRepository.save(group);
        return convertToDTO(savedGroup);
    }

    public List<GroupDTO> getAllGroups() {
        return groupRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public GroupDTO getGroupById(Long id) {
        return groupRepository.findById(id)
                .map(this::convertToDTO)
                .orElse(null);
    }

    public GroupDTO updateGroup(Long id, GroupDTO groupDTO) {
        return groupRepository.findById(id)
                .map(existingGroup -> {
                    existingGroup.setName(groupDTO.getName());
                    existingGroup.setDescription(groupDTO.getDescription());
                    Group updatedGroup = groupRepository.save(existingGroup);
                    return convertToDTO(updatedGroup);
                })
                .orElse(null);
    }

    public void deleteGroup(Long id) {
        groupRepository.deleteById(id);
    }

    private GroupDTO convertToDTO(Group group) {
        GroupDTO groupDTO = new GroupDTO();
        groupDTO.setId(group.getId());
        groupDTO.setName(group.getName());
        groupDTO.setDescription(group.getDescription());
        return groupDTO;
    }
}