package com.certsniper.planner;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;

import com.certsniper.planner.repository.PersonalScheduleRepository;
import com.certsniper.planner.repository.PlannerBarRepository;
import com.certsniper.planner.repository.PlannerMilestoneRepository;
import com.certsniper.planner.repository.PlannerRepository;
import com.certsniper.planner.service.PlannerService;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

class PlannerServiceTest {

    private final PlannerRepository plannerRepo = mock(PlannerRepository.class);
    private final PlannerBarRepository barRepo = mock(PlannerBarRepository.class);
    private final PlannerMilestoneRepository milestoneRepo = mock(PlannerMilestoneRepository.class);
    private final PersonalScheduleRepository personalRepo = mock(PersonalScheduleRepository.class);
    private final PlannerService service = new PlannerService(plannerRepo, barRepo, milestoneRepo, personalRepo);

    @Test
    void 탈퇴하면_일정바_개인일정_플래너_순서로_삭제() {
        service.deleteAllByUser(1L);

        InOrder order = inOrder(barRepo, personalRepo, plannerRepo);
        order.verify(barRepo).deleteAllByUserId(1L);
        order.verify(personalRepo).deleteAllByUserId(1L);
        order.verify(plannerRepo).deleteAllByUserId(1L);
    }
}
