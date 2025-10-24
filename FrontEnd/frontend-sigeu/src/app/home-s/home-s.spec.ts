import { ComponentFixture, TestBed } from '@angular/core/testing';

import { HomeS } from './home-s';

describe('HomeS', () => {
  let component: HomeS;
  let fixture: ComponentFixture<HomeS>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [HomeS]
    })
    .compileComponents();

    fixture = TestBed.createComponent(HomeS);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
