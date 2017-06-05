package edu.uw.citw.service;

import edu.uw.citw.persistence.repository.NetworkDataRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;


@Service
public class LaughNetworkDataService {

    private static final Logger log = LoggerFactory.getLogger(LaughNetworkDataService.class);

    private static final String CSV_SEP = ",";

    private NetworkDataRepository repository;

    public LaughNetworkDataService(NetworkDataRepository repository) {
        this.repository = repository;
    }

    /**
     * Go through and append person laughter's from, laughter's to, and percentage of participation over presence
     *
     * Using a custom query, so no POJO to represent the values.
     */
    public String findPersonEngagementEdges_CSV() {
        StringBuilder b = new StringBuilder("");
        b.append("from,to,weight\n");
        try {
            List<Object[]> vals = repository.getLaughPeopleRatios();
            for (Object[] val : vals) {
                b.append((String) val[0])
                        .append(CSV_SEP)
                        .append((String) val[1])
                        .append(CSV_SEP)
                        .append((BigDecimal) val[2])
                        .append('\n');
            }
        } catch (Exception e) {
            log.error("Failure retrieving from database", e);
        }
        return b.toString();
    }

    public String findHumorEngagementEdges_CSV() {
        StringBuilder b = new StringBuilder("");
        b.append("from,to,weight\n");
        try {
            List<Object[]> vals = repository.getLaughHumorRatios();
            for (Object[] val : vals) {
                b.append((String) val[0])
                        .append(CSV_SEP)
                        .append((String) val[1])
                        .append(CSV_SEP)
                        .append((BigDecimal) val[2])
                        .append('\n');
            }
        } catch (Exception e) {
            log.error("Failure retrieving from database", e);
        }
        return b.toString();

    }

    public String getAllNodes_peopleAndHumor_CSV() {
        StringBuilder b = new StringBuilder("");
        b.append("name,type\n");
        try {
            List<Object[]> vals = repository.getBipartiteNodes_peopleAndHumor();
            for (Object[] val : vals) {
                b.append((String) val[0])
                        .append(CSV_SEP)
                        .append((String) val[1])
                        .append('\n');
            }
        } catch (Exception e) {
            log.error("Failure retrieving from database", e);
        }
        return b.toString();

    }
}
