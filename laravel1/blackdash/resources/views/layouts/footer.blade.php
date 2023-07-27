<footer class="footer">
    <div class="container-fluid">
        <ul class="nav">
            <li class="nav-item">
                <a href="https://creative-tim.com" target="blank" class="nav-link">
                    {{ __('UPRISE') }}
                </a>
            </li>
            <li class="nav-item">
                <a href="https://updivision.com" target="blank" class="nav-link">
                    {{ __('SACCO') }}
                </a>
            </li>
            <li class="nav-item">
                <a href="#" class="nav-link">
                    {{ __('About Us') }}
                </a>
            </li>
            <li class="nav-item">
                <a href="#" class="nav-link">
                    {{ __('Blog') }}
                </a>
            </li>
        </ul>
        <div class="copyright">
            &copy; {{ now()->year }} {{ __('made with') }} <i class="tim-icons icon-heart-2"></i> {{ __('by') }}
            <a href="https://creative-tim.com" target="_blank">{{ __('ARINDA') }}</a> &amp;
            <a href="https://updivision.com" target="_blank">{{ __('ASIIMWE') }}</a> {{ __('ATEWTA') }}.
        </div>
    </div>
</footer>

{{-- REQUIRED SCRIPTS --}}
<!-- jQuery -->
<script src="{{ asset('plugins/jquery/jquery.min.js')}}"></script> 
<!-- AdminLTE -->
<script src="{{ asset('dist/js/adminlte.js')}}"></script> 


{{-- <!-- jQuery -->
<script src="{{ asset('plugins/jquery/jquery.min.js')}}"></script>
<!-- Bootstrap -->
<script src="{{ asset('plugins/bootstrap/js/bootstrap.bundle.min.js')}}"></script>
<!-- AdminLTE -->
<script src="{{ asset('dist/js/adminlte.js')}}"></script> --}}

<!-- OPTIONAL SCRIPTS -->
<script src="{{ asset('plugins/chart.js/Chart.min.js')}}"></script>
<!-- AdminLTE for demo purposes -->
<script src="{{ asset('dist/js/demo.js')}}"></script>
<!-- AdminLTE dashboard demo (This is only for demo purposes) -->
<script src="{{ asset('dist/js/pages/dashboard3.js')}}"></script>