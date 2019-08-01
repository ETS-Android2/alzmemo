package com.main.alz2.pojo;

/**
 * Created by Jeff on 12/29/2015.
 */
public class Memory {

    int memoryId;
    int relationId;
    String file1;
    String file2;
    String file3;
    String file4;
    String file5;

    public Memory(int memoryId, int relationId, String file1, String file2, String file3, String file4, String file5) {
        this.memoryId = memoryId;
        this.relationId = relationId;
        this.file1 = file1;
        this.file2 = file2;
        this.file3 = file3;
        this.file4 = file4;
        this.file5 = file5;
    }

    public String getMemory(int index){
        switch(index){
            case 1: return this.file1;
            case 2: return this.file2;
            case 3: return this.file3;
            case 4: return this.file4;
            default: return this.file5;
        }
    }

    public int getMemoryId() {
        return memoryId;
    }

    public void setMemoryId(int memoryId) {
        this.memoryId = memoryId;
    }

    public int getRelationId() {
        return relationId;
    }

    public void setRelationId(int relationId) {
        this.relationId = relationId;
    }

    public String getFile1() {
        return file1;
    }

    public void setFile1(String file1) {
        this.file1 = file1;
    }

    public String getFile2() {
        return file2;
    }

    public void setFile2(String file2) {
        this.file2 = file2;
    }

    public String getFile3() {
        return file3;
    }

    public void setFile3(String file3) {
        this.file3 = file3;
    }

    public String getFile4() {
        return file4;
    }

    public void setFile4(String file4) {
        this.file4 = file4;
    }

    public String getFile5() {
        return file5;
    }

    public void setFile5(String file5) {
        this.file5 = file5;
    }
}
