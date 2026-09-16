import { TestBed } from '@angular/core/testing';
import { ChangeSummaryView } from './change-summary';
import { ChangeSummary } from '../../models/template-interface';
import { ITEMS_ENG_1007 } from '../../models/template-constants';

/** Why: the user must be able to tell "no changes" apart from "not computed yet", and see accumulated history. */
describe('ChangeSummaryView', () => {
  async function render(summary: ChangeSummary, showVersions: boolean): Promise<HTMLElement> {
    const fixture = TestBed.createComponent(ChangeSummaryView);
    fixture.componentRef.setInput('summary', summary);
    fixture.componentRef.setInput('showVersions', showVersions);
    await fixture.whenStable();
    return fixture.nativeElement as HTMLElement;
  }

  it('shows a computing message rather than an empty list while the summary is not ready', async () => {
    const el = await render(
      { availability: 'COMPUTING', fromVersion: 11, toVersion: 12, items: [], computedAt: null, unavailableReason: null },
      false,
    );
    expect(el.textContent).toContain('being prepared');
    expect(el.textContent).not.toContain('no content changes');
    expect(el.querySelectorAll('li').length).toBe(0);
  });

  it('groups accumulated changes by area and shows which versions touched each one', async () => {
    const el = await render(
      { availability: 'READY', fromVersion: 6, toVersion: 8, items: ITEMS_ENG_1007, computedAt: '2026-09-15T09:00:00Z', unavailableReason: null },
      true,
    );
    const areas = Array.from(el.querySelectorAll('h4')).map((h) => h.textContent?.trim());
    expect(areas).toEqual(['Template details', 'Inquiries', 'Analytics', 'Completion']);

    const tolerance = Array.from(el.querySelectorAll('li')).find((li) => li.textContent?.includes('tolerance'))!;
    expect(tolerance.textContent).toContain('Before: 0.15');
    expect(tolerance.textContent).toContain('After: 0.1');
    expect(tolerance.querySelector('.versions')?.textContent).toContain('v7, v8');
  });
});
