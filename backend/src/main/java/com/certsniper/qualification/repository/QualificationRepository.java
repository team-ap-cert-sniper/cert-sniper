package com.certsniper.qualification.repository;

import com.certsniper.qualification.entity.Qualification;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface QualificationRepository extends JpaRepository<Qualification, String> {

    /** 직무분야(대·중)별 종목 수. qualgbcd가 null이면 전체 */
    @Query("select q.obligfldcd as obligfldcd, q.mdobligfldcd as mdobligfldcd, count(q) as total"
            + " from Qualification q where :qualgbcd is null or q.qualgbcd = :qualgbcd"
            + " group by q.obligfldcd, q.mdobligfldcd")
    List<FieldCount> countByField(String qualgbcd);

    interface FieldCount {
        String getObligfldcd();

        String getMdobligfldcd();

        long getTotal();
    }
}
