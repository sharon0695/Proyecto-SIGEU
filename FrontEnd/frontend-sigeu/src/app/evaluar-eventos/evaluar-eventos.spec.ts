import { ComponentFixture, TestBed } from '@angular/core/testing';

import { EvaluarEventos } from './evaluar-eventos';

describe('EvaluarEventos', () => {
  let component: EvaluarEventos;
  let fixture: ComponentFixture<EvaluarEventos>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [EvaluarEventos]
    })
    .compileComponents();

    fixture = TestBed.createComponent(EvaluarEventos);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
