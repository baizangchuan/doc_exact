package org.example;

import java.util.Optional;

public class Args {
    private String content;
    private String hospital;
    private String tml_type;
    private boolean multi_task;
    private Optional<String> record_type;
    private boolean extract_title;
    private boolean load_latest_config;

    // 这里省略了 getters 和 setters
    public String getContent() {
        return content;
    }
    public void setContent(String content) {
        this.content = content;
    }

    public String getHospital() { return hospital; }
    public void setHospital(String hospital) { this.hospital = hospital; }

    public String getTml_type() { return tml_type; }
    public void setTml_type(String tml_type) { this.tml_type = tml_type; }

    public boolean isMulti_task() { return multi_task; }
    public void setMulti_task(boolean multi_task) { this.multi_task = multi_task; }

    public Optional<String> getRecord_type() { return record_type; }
    public void setRecord_type(Optional<String> record_type) { this.record_type = record_type; }
    public boolean isExtract_title() { return extract_title; }
    public void setExtract_title(boolean extract_title) { this.extract_title = extract_title; }
    public boolean isLoad_latest_config() { return load_latest_config; }
    public void setLoad_latest_config(boolean load_latest_config) {this.load_latest_config=load_latest_config; }
}
