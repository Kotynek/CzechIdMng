import chai, { expect } from 'chai';
import dirtyChai from 'dirty-chai';
chai.use(dirtyChai);

import UiUtils from '../../src/utils/UiUtils';

/**
 * Util.Ui. method tests
 *
 * @author Marek Klement
 * @author Radek Tomiška
 */

describe('UiUtils', function testUiUtils() {
  describe('[substringBegin]', function testSubstringBegin() {
    //
    it('- tests of cutting in the end', function test() {
      expect(UiUtils.substringBegin('ahojj/j/', 5, '/')).to.equal('ahojj');
    });

    it('- basic function - 10 chars', function test() {
      expect(UiUtils.substringBegin('ahojjj/jjhjhjgj/', 10, '/')).to.equal('ahojjj');
    });

    it('- basic function - 4 chars', function test() {
      expect(UiUtils.substringBegin('ahojj/j/', 4, '/')).to.equal('');
    });

    it('- of position of cutChar same as needed length', function test() {
      expect(UiUtils.substringBegin('ahojj/j/', 6, '/')).to.equal('ahojj');
    });

    it('- of low number wit cutChar at begining', function test() {
      expect(UiUtils.substringBegin('/ahojj/j/', 2, '/')).to.equal('');
    });

    it('- begin with blank text', function test() {
      expect(UiUtils.substringBegin('', 5, '/')).to.equal('');
    });

    it('- begin for null', function test() {
      expect(UiUtils.substringBegin(null, 2, '/')).to.equal(null);
    });
  });

  describe('[substringEnd]', function testSubstringEnd() {
    //
    it('- of position of cutChar higher than needed length', function test() {
      expect(UiUtils.substringEnd('ahojj/j/', 4, '/')).to.equal('/j/');
    });

    it('- of position of cutChar same as needed length', function test() {
      expect(UiUtils.substringEnd('ahojj/j/', 3, '/')).to.equal('/j/');
    });

    it('- of position of cutChar lower than needed length', function test() {
      expect(UiUtils.substringEnd('ahojj/j/', 2, '/')).to.equal('/');
    });

    it('- end with blank text', function test() {
      expect(UiUtils.substringEnd('', 2, '/')).to.equal('/');
    });

    it('- end for null', function test() {
      expect(UiUtils.substringEnd(null, 2, '/')).to.equal(null);
    });
  });

  // TODO ...
  it('- getRowClass');
  it('- getDisabledRowClass');
  it('- getSimpleJavaType');
});
