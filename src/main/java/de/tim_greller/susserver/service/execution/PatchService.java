package de.tim_greller.susserver.service.execution;

import java.util.LinkedList;

import name.fraser.neil.plaintext.diff_match_patch;
import org.springframework.stereotype.Service;

@Service
public class PatchService {

    diff_match_patch patchLibrary = new diff_match_patch();

    public String createPatch(String oldSource, String newSource) {
        var patch = patchLibrary.patch_make(oldSource, newSource);
        return patchLibrary.patch_toText(patch);
    }

    public String applyPatch(String oldSource, String patch) {
        var patches = new LinkedList<>(patchLibrary.patch_fromText(patch));
        var result = patchLibrary.patch_apply(patches, oldSource);
        return (String) result[0];
    }

}
