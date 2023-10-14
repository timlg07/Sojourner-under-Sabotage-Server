package de.tim_greller.susserver.service.execution;

import name.fraser.neil.plaintext.diff_match_patch;
import org.springframework.stereotype.Service;

@Service
public class PatchService {

    diff_match_patch patchLibrary = new diff_match_patch();

    public String createPatch(String oldSource, String newSource) {
        var patch = patchLibrary.patch_make(oldSource, newSource);
        return patchLibrary.patch_toText(patch);
    }

}
