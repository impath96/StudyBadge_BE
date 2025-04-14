package com.tenten.studybadge.study.channel.service;

import com.tenten.studybadge.study.channel.domain.entity.StudyChannel;
import org.springframework.data.jpa.domain.Specification;

public class StudyChannelSpecification {

    public static Specification<StudyChannel> withRecruitmentStatus(String recruitmentStatus) {
        return (root, query, cb) -> cb.equal(root.get("recruitment").get("recruitmentStatus"), recruitmentStatus);
    }

    public static Specification<StudyChannel> withCategory(String category) {
        return (root, query, cb) -> cb.equal(root.get("category"), category);
    }

    public static Specification<StudyChannel> withMeetingType(String meetingType) {
        return (root, query, cb) -> cb.equal(root.get("meetingType"), meetingType);
    }

}