package com.tenten.studybadge.common.batch;

import com.tenten.studybadge.member.domain.entity.Member;
import com.tenten.studybadge.member.domain.repository.MemberRepository;
import com.tenten.studybadge.point.domain.entity.Point;
import com.tenten.studybadge.point.domain.repository.PointRepository;
import com.tenten.studybadge.study.deposit.domain.entity.StudyChannelDeposit;
import com.tenten.studybadge.study.deposit.domain.repository.StudyChannelDepositRepository;
import com.tenten.studybadge.type.point.PointHistoryType;
import com.tenten.studybadge.type.point.TransferType;
import com.tenten.studybadge.type.study.deposit.DepositStatus;
import com.tenten.studybadge.type.study.member.StudyMemberStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class AttendanceRatioWriter implements ItemWriter<StudyMemberAttendanceRatioList> {

    private final StudyChannelDepositRepository depositRepository;
    private final PointRepository pointRepository;
    private final MemberRepository memberRepository;

    @Override
    public void write(Chunk<? extends StudyMemberAttendanceRatioList> chunk) throws Exception {
        List<? extends StudyMemberAttendanceRatioList> items = chunk.getItems();

        items.forEach(item -> {
            List<StudyMemberAttendanceRatio> ratios = item.getStudyMemberAttendanceRatioList();
            List<Long> studyMemberIds = ratios.stream().map(StudyMemberAttendanceRatio::getStudyMemberId).toList();
            List<StudyChannelDeposit> deposits = depositRepository.findAllByStudyMemberIdIn(studyMemberIds);
            Map<Long, StudyChannelDeposit> depositMap = deposits.stream().collect(Collectors.toMap((deposit) -> deposit.getStudyMember().getId(), Function.identity()));

            int sum = deposits.stream().mapToInt(StudyChannelDeposit::getAmount).sum();
            double totalAttendanceRatio = ratios.stream().mapToDouble(StudyMemberAttendanceRatio::getAttendanceRatio).sum();

            for (StudyMemberAttendanceRatio ratio : ratios) {
                StudyChannelDeposit deposit = depositMap.get(ratio.getStudyMemberId());
                if (deposit.getStudyMember().getStudyMemberStatus() != StudyMemberStatus.PARTICIPATING) {
                    continue;
                }
                int myRefunds = Double.valueOf(Math.floor(sum * ratio.getAttendanceRatio() / totalAttendanceRatio)).intValue();

                deposit.setAttendanceRatio(ratio.getAttendanceRatio());
                deposit.setDepositStatus(DepositStatus.REFUND);
                deposit.setRefundsAmount(myRefunds);
                deposit.setAmount(0);
                Member member = deposit.getMember();
                Point point = Point.builder()
                        .transferType(TransferType.STUDY_REWARD)
                        .historyType(PointHistoryType.EARNED)
                        .member(member)
                        .amount(myRefunds)
                        .build();
                Member updatedMember = member.toBuilder()
                        .point(member.getPoint() + myRefunds)
                        .build();
                memberRepository.save(updatedMember);
                depositRepository.save(deposit);
                pointRepository.save(point);
            }
        });

    }
}
