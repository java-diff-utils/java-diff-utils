package com.github.difflib.patch;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.github.difflib.DiffUtils;
import java.util.ArrayList;

public class PatchTest {

    @Test
    public void testPatch_Insert() {
        final List<String> insertTest_from = Arrays.asList("hhh");
        final List<String> insertTest_to = Arrays.asList("hhh", "jjj", "kkk", "lll");

        final Patch<String> patch = DiffUtils.diff(insertTest_from, insertTest_to);
        try {
            assertEquals(insertTest_to, DiffUtils.patch(insertTest_from, patch));
        } catch (PatchFailedException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testPatch_Delete() {
        final List<String> deleteTest_from = Arrays.asList("ddd", "fff", "ggg", "hhh");
        final List<String> deleteTest_to = Arrays.asList("ggg");

        final Patch<String> patch = DiffUtils.diff(deleteTest_from, deleteTest_to);
        try {
            assertEquals(deleteTest_to, DiffUtils.patch(deleteTest_from, patch));
        } catch (PatchFailedException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testPatch_Change() {
        final List<String> changeTest_from = Arrays.asList("aaa", "bbb", "ccc", "ddd");
        final List<String> changeTest_to = Arrays.asList("aaa", "bxb", "cxc", "ddd");

        final Patch<String> patch = DiffUtils.diff(changeTest_from, changeTest_to);
        try {
            assertEquals(changeTest_to, DiffUtils.patch(changeTest_from, patch));
        } catch (PatchFailedException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testPatch_Serializable() throws IOException, ClassNotFoundException {
        final List<String> changeTest_from = Arrays.asList("aaa", "bbb", "ccc", "ddd");
        final List<String> changeTest_to = Arrays.asList("aaa", "bxb", "cxc", "ddd");

        final Patch<String> patch = DiffUtils.diff(changeTest_from, changeTest_to);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(baos);
        out.writeObject(patch);
        out.close();
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream in = new ObjectInputStream(bais);
        Patch<String> result = (Patch<String>) in.readObject();
        in.close();

        try {
            assertEquals(changeTest_to, DiffUtils.patch(changeTest_from, result));
        } catch (PatchFailedException e) {
            fail(e.getMessage());
        }

    }

    @Test
    public void testPatch_Change_withExceptionProcessor() {
        final List<String> changeTest_from = Arrays.asList("aaa", "bbb", "ccc", "ddd");
        final List<String> changeTest_to = Arrays.asList("aaa", "bxb", "cxc", "ddd");

        final Patch<String> patch = DiffUtils.diff(changeTest_from, changeTest_to);

        changeTest_from.set(2, "CDC");

        patch.withConflictOutput(new ConflictOutput<String>() {
            @Override
            public void processConflict(VerifyChunk verifyChunk, AbstractDelta<String> delta, List<String> result) throws PatchFailedException {
                if (result.size() > delta.getSource().getPosition()) {
                    List<String> orgData = new ArrayList<>();

                    for (int i = 0; i < delta.getSource().size(); i++) {
                        orgData.add(result.get(delta.getSource().getPosition()));
                        result.remove(delta.getSource().getPosition());
                    }

                    orgData.add(0, "<<<<<< HEAD");
                    orgData.add("======");
                    orgData.addAll(delta.getSource().getLines());
                    orgData.add(">>>>>>> PATCH");
                    
                    result.addAll(delta.getSource().getPosition(), orgData);

                } else {
                    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
                }
            }
        });

        try {
            List<String> data = DiffUtils.patch(changeTest_from, patch);
            assertEquals(9, data.size());
            
            assertEquals(Arrays.asList("aaa", "<<<<<< HEAD", "bbb", "CDC", "======", "bbb", "ccc", ">>>>>>> PATCH", "ddd"), data);
            
        } catch (PatchFailedException e) {
            fail(e.getMessage());
        }
    }
}
