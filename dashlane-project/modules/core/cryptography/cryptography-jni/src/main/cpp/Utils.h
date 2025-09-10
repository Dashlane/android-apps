using namespace std;

#pragma once

#include <jni.h>
#include <string>

/**
 * Util methods to get static values
 *
 * Migrated from desktop code
 * https://stash.dashlane.com/projects/CLIENT/repos/desktop/browse/CppLibrairies/SourceCode/KWUtils/Utils/KWUtil.cpp
 */
class Utils {

public:

    static std::string get_value() {
        try {
            char string[] = "+q@$0640{*/1IFQ-fyiAWcVc29#&mJ";


            string[12] = string[3];
            string[2]--;

            memcpy(&string[13], &string[5], 3);
            string[13] = string[14];
            string[14] += 2;
            memcpy(&string[15], &string[24], 1);

            std::string obf(&string[10], 7);
            obf.replace(0, 2, "7t");

            std::string hid("kw2");
            hid.append(obf);
            hid.insert(3, get_miscValue());
            return hid;
        } catch (std::exception *e) {}
        catch (std::exception &e) {}
        catch (...) {}

        return NULL;
    }

    static std::string get_miscValue() {
        char string[] = "+q@$?640{*/1IFQ-fyiAWcVc29#&mJ";

        string[2] = string[5]--;
        string[2]--;
        memcpy(&string[3], &string[12], 2);
        std::string obf(&string[2], 3);
        obf.append(std::string(&string[25], 1));
        return obf;
    }
};
