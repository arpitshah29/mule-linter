package com.avioconsulting.mule.linter.rule.readme

import com.avioconsulting.mule.linter.model.Application
import com.avioconsulting.mule.linter.model.ReadmeFile
import com.avioconsulting.mule.linter.model.rule.Rule
import com.avioconsulting.mule.linter.model.rule.RuleViolation

/**
 * This rule checks for the existence of a `README.md` file at the root of the project.
 * Projects should have a README in the project, explaining the purpose and content of the project.
 */
class ReadmeRule extends Rule {

    static final String RULE_ID = 'README'
    static final String RULE_NAME = 'The README.md exists and has content. '
    static final String RULE_VIOLATION_MESSAGE = 'The README.md has no content'
    static final String FILE_MISSING_VIOLATION_MESSAGE = 'The README.md does not exist'

    ReadmeRule() {
        super(RULE_ID, RULE_NAME)
    }

    @Override
    List<RuleViolation> execute(Application app) {
        List<RuleViolation> violations = []

        ReadmeFile readmeFile = app.readmeFile
        if (readmeFile.doesExist()) {
            if (readmeFile.getSize() == 0) {
                violations.add(new RuleViolation(this, readmeFile.path,
                        0, RULE_VIOLATION_MESSAGE))
            }
        } else {
            violations.add(new RuleViolation(this, readmeFile.path,
                    0, FILE_MISSING_VIOLATION_MESSAGE))
        }
        return violations
    }
}
